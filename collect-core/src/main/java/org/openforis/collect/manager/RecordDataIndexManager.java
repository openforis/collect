/**
 * 
 */
package org.openforis.collect.manager;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Configuration;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.Record;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class RecordDataIndexManager {
	
	public enum SearchType {
		EQUAL, STARTS_WITH, CONTAINS;
	}

	private static Log LOG = LogFactory.getLog(RecordDataIndexManager.class);

	protected static final String INDEX_PATH_CONFIGURATION_KEY = "index_path";
	
	protected static final QName INDEX_NAME_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/collect", "index");

	private static final String RECORD_ID_FIELD = "_record_id";
	
	protected RAMDirectory ramDirectory;
	
	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private RecordManager recordManager;
	
	private String indexRootPath;
	
	protected void init() throws Exception {
		Configuration configuration = configurationManager.getConfiguration();
		indexRootPath = configuration.get(INDEX_PATH_CONFIGURATION_KEY);
		initTemporaryIndex();
	}

	public void initTemporaryIndex() throws RecordDataIndexException, CorruptIndexException, IOException {
		ramDirectory = new RAMDirectory();
		IndexWriter indexWriter = createTemporaryIndexWriter();
		indexWriter.close();
	}

	public void destroyIndex() throws RecordDataIndexException {
		IndexWriter indexWriter = createIndexWriter();
		destroyIndex(indexWriter);
	}
	
	public void destroyTemporaryIndex() throws RecordDataIndexException {
		IndexWriter indexWriter = createTemporaryIndexWriter();
		destroyIndex(indexWriter);
	}
	
	protected void destroyIndex(IndexWriter indexWriter) throws RecordDataIndexException {
		try {
			indexWriter.deleteAll();
		} catch (Exception e) {
			throw new RecordDataIndexException(e);
		} finally {
			closeIndexHandler(indexWriter);
		}
	}

	public void temporaryIndex(CollectRecord record) throws RecordDataIndexException {
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		if ( hasIndexableNodes(rootEntityDefn) ) {
			IndexWriter indexWriter = null;
			try {
				indexWriter = createTemporaryIndexWriter();
				indexWriter.deleteAll(); //temporary index is relative only to one record
				index(indexWriter, record);
			} catch (Exception e) {
				throw new RecordDataIndexException(e);
			} finally {
				closeIndexHandler(indexWriter);
			}
		}
	}
	
	public void index(CollectRecord record) throws RecordDataIndexException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = createIndexWriter();
			Integer recordId = record.getId();
			deleteDocuments(indexWriter, recordId);
			index(indexWriter, record);
		} catch (Exception e) {
			throw new RecordDataIndexException(e);
		} finally {
			closeIndexHandler(indexWriter);
		}
	}
	
	public void indexAllRecords(CollectSurvey survey, String rootEntity) throws RecordDataIndexException {
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntity);
		IndexWriter indexWriter = null;
		try {
			indexWriter = createIndexWriter();
			for (CollectRecord record : summaries) {
				Integer recordId = record.getId();
				deleteDocuments(indexWriter, recordId);
				index(indexWriter, record);
			}
		} catch (Exception e) {
			throw new RecordDataIndexException(e);
		} finally {
			closeIndexHandler(indexWriter);
		}
	}

	public boolean hasIndexableNodes(Survey survey) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition entityDefn: rootEntityDefinitions) {
			boolean hasIndexableNodes = hasIndexableNodes(entityDefn);
			if ( hasIndexableNodes ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasIndexableNodes(EntityDefinition entityDefn) {
		Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
		stack.push(entityDefn);
		while ( ! stack.isEmpty() ) {
			NodeDefinition defn = stack.pop();
			String indexName = defn.getAnnotation(INDEX_NAME_ANNOTATION);
			if ( StringUtils.isNotBlank(indexName) ) {
				return true;
			}
			if ( defn instanceof EntityDefinition ) {
				List<NodeDefinition> childDefns = ((EntityDefinition) defn).getChildDefinitions();
				stack.addAll(childDefns);
			}
		}
		return false;
	}
	
	private void index(final IndexWriter indexWriter, CollectRecord record) throws RecordDataIndexException {
		try {
			Entity rootEntity = record.getRootEntity();
			rootEntity.traverse(new NodeVisitor() {
				@Override
				public void visit(Node<? extends NodeDefinition> node, int idx) {
					NodeDefinition defn = node.getDefinition();
					if (defn instanceof AttributeDefinition ) {
						index(indexWriter, (Attribute<?, ?>) node);
					}
				}
			});
		} catch (Exception e) {
			throw new RecordDataIndexException(e);
		}
	}

	public void index(List<CollectRecord> records) throws Exception {
		for (CollectRecord record : records) {
			index(record);
		}
	}

    public List<String> search(SearchType searchType, Survey survey, int attributeDefnId, int fieldIndex, String queryText, int maxResults)  throws RecordDataIndexException {
    	Schema schema = survey.getSchema();
    	AttributeDefinition defn = (AttributeDefinition) schema.getById(attributeDefnId);
    	String indexName = defn.getAnnotation(INDEX_NAME_ANNOTATION);
		if ( StringUtils.isNotBlank(indexName) ) {
			IndexSearcher indexSearcher = null;
			try {
				//search in ram directory
				indexSearcher = createTemporaryIndexSearcher();
				Set<String> tempResult = search(indexName, indexSearcher, searchType, queryText, fieldIndex, maxResults);
				//search in file system index
		        indexSearcher = createIndexSearcher();
		        Set<String> committedResult = search(indexName, indexSearcher, searchType, queryText, fieldIndex, maxResults);
		        List<String> result = mergeSearchResults(maxResults, tempResult, committedResult);
		        return result;
	        } catch(Exception e) {
	        	throw new RecordDataIndexException(e);
	        } finally {
	        	closeIndexHandler(indexSearcher);
	        }
		} else {
			throw new RecordDataIndexException("");
		}
    }

	protected void deleteDocuments(IndexWriter indexWriter, Integer recordId)
			throws RecordDataIndexException {
		try {
			Term term = new Term(RECORD_ID_FIELD, recordId != null ? recordId.toString(): "null");
			indexWriter.deleteDocuments(term);
		} catch (Exception e) {
			throw new RecordDataIndexException(e);
		}
	}

	protected List<String> mergeSearchResults(int maxResults, Set<String> tempResult, Set<String> committedResult) {
		Set<String> result = new HashSet<String>();
		result.addAll(tempResult);
		result.addAll(committedResult);
		List<String> sortedList = new ArrayList<String>(result);
		Collections.sort(sortedList);
		if ( sortedList.size() > maxResults ) {
			sortedList = sortedList.subList(0, maxResults - 1);
		}
		return sortedList;
	}

	protected Set<String> search(String indexName, IndexSearcher indexSearcher, SearchType searchType, String queryText, int fieldIndex, int maxResults)
			throws Exception {
		Set<String> result = new HashSet<String>();
        if ( indexSearcher != null ) {
			String indexFieldKey = indexName + "_" +Integer.toString(fieldIndex);
			Query query = createQuery(searchType, indexFieldKey, queryText);
			TopDocs hits = indexSearcher.search(query, maxResults);
			ScoreDoc[] scoreDocs = hits.scoreDocs;
			for (ScoreDoc scoreDoc : scoreDocs) {
			    Document doc = indexSearcher.doc(scoreDoc.doc);
			    String value = doc.get(indexFieldKey);
				result.add(value);
			}
        }
		return result;
	}
    
    protected IndexWriter createTemporaryIndexWriter() throws RecordDataIndexException {
    	try {
    		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
			IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, analyzer);
			conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
			IndexWriter indexWriter = new IndexWriter(ramDirectory, conf);
	    	return indexWriter;
    	} catch (IOException e) {
			throw new RecordDataIndexException(e.getMessage(), e);
		}
    }

	protected IndexWriter createIndexWriter() throws RecordDataIndexException {
		try {
			File indexDir = new File(indexRootPath);
			if ( indexDir.exists() || indexDir.mkdirs() ) {
				StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
				IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, analyzer);
				conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
				Directory directory = new SimpleFSDirectory(indexDir);
				IndexWriter indexWriter = new IndexWriter(directory, conf);
				return indexWriter;
			} else {
				throw new RecordDataIndexException("Cannot access index directory: " + indexRootPath);
			}
		} catch (IOException e) {
			throw new RecordDataIndexException(e.getMessage(), e);
		}
	}

	protected IndexSearcher createIndexSearcher() throws RecordDataIndexException {
		try {
			File indexDir = new File(indexRootPath);
	        if ( indexDir.exists() || indexDir.mkdirs() ) {
				Directory directory = new SimpleFSDirectory(indexDir);
		        IndexReader indexReader = IndexReader.open(directory);
				int numDocs = indexReader.numDocs();
				if ( numDocs > 0 ) {
			        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			        return indexSearcher;
				} else {
					return null;
				}
	        } else {
	        	throw new RecordDataIndexException("Cannot access index directory: " + indexRootPath);
	        }
		} catch (IOException e) {
			throw new RecordDataIndexException(e.getMessage(), e);
		}
    }
	
	protected IndexSearcher createTemporaryIndexSearcher() throws RecordDataIndexException {
		try {
			IndexReader indexReader = IndexReader.open(ramDirectory);
	        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
	        return indexSearcher;
		} catch (IOException e) {
			throw new RecordDataIndexException(e.getMessage(), e);
		}
	}

	protected void index(IndexWriter indexWriter, Attribute<?, ?> attr) {
		AttributeDefinition defn = attr.getDefinition();
		String indexName = defn.getAnnotation(INDEX_NAME_ANNOTATION);
		if ( StringUtils.isNotBlank(indexName) ) {
			try {
				Object value = attr.getValue();
				if ( value != null ) {
					Record record = attr.getRecord();
					Integer recordId = record.getId();
					Document doc = new Document();
					Field recordKeyField = createRecordIdField(recordId);
					doc.add(recordKeyField);
					int fieldCount = attr.getFieldCount();
					for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++ ) {
						org.openforis.idm.model.Field<?> field = attr.getField(fieldIndex);
						index(doc, indexName, field);
					}
					indexWriter.addDocument(doc);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void index(Document doc, String indexName, org.openforis.idm.model.Field<?> field) {
		int fieldIndex = field.getIndex();
		Object fieldValue = field.getValue();
		if ( fieldValue != null ) {
			String fieldValueStr = fieldValue.toString();
			if ( StringUtils.isNotBlank(fieldValueStr)) {
				String fieldKey = indexName + "_" + Integer.toString(fieldIndex);
				Field docField = new Field(fieldKey, fieldValueStr, Field.Store.YES, Field.Index.ANALYZED);
				doc.add(docField);
			}
		}
	}
	
	protected Field createRecordIdField(Integer recordId) {
		Field recordKeyField = new Field(RECORD_ID_FIELD, recordId != null ? recordId.toString(): "null", Field.Store.YES, Field.Index.NOT_ANALYZED);
		return recordKeyField;
	}

	protected Query createQuery(SearchType searchType, String indexFieldKey, String searchText) throws ParseException {
		String escapedSearchText = QueryParser.escape(searchText.toLowerCase());
		//String escapedSearchText = searchText.toLowerCase();
		String queryText;
		switch ( searchType ) {
		case STARTS_WITH:
			queryText = escapedSearchText + "*";
			break;
		case CONTAINS:
			queryText = "*" + escapedSearchText + "*";
			break;
		default:
			queryText = escapedSearchText;
		}
		Term term = new Term(indexFieldKey, queryText);
        Query query = new WildcardQuery(term);
        /*
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		QueryParser queryParser = new QueryParser(Version.LUCENE_35, queryText, analyzer);
		queryParser.setDefaultOperator(Operator.AND);
		Query query = queryParser.parse(queryText);
		*/
		//This is wild card query, to use Fuzzy Query use following
        /*
         Term term = new Term(key,queryText+"~");
        Query  query = new FuzzyQuery(term,0.4F); 0.4 is 40 % matching with queryText
         * */
        return query;
    }
	
//	private void listAllDocs() throws RecordDataIndexException {
//		IndexSearcher indexSearcher = null;
//		try {
//			indexSearcher = createIndexSearcher();
//			IndexReader reader = indexSearcher.getIndexReader();
//			for (int i=0; i<reader.maxDoc(); i++) {
//			    if (reader.isDeleted(i))
//			        continue;
//			    Document doc = reader.document(i);
//			    System.out.println(doc.toString());
//			}
//		} catch(Exception e) {
//			throw new RecordDataIndexException(e);
//		} finally {
//			closeIndexHandler(indexSearcher);
//		}
//	}
	
	private void closeIndexHandler(Closeable indexHandler) {
		if ( indexHandler != null ) {
			try {
				indexHandler.close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/*
	public static void main(String[] args) throws IOException, ParseException {
		// 0. Specify the analyzer for tokenizing text.
		//    The same analyzer should be used for indexing and searching
		//StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		Analyzer analyzer = new KeywordAnalyzer();
		//Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_35);
		// 1. create the index
		Directory index = new RAMDirectory();

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);

		IndexWriter w = new IndexWriter(index, config);
		addDoc(w, "Lucene in Action");
		addDoc(w, "Lucene for Dummies");
		addDoc(w, "Managing Gigabytes");
		addDoc(w, "The Art of Computer Science");
		w.close();

		// 2. query
		while(true)
			search(index);
	}
	
	private static void search(Directory index) throws IOException, ParseException, CorruptIndexException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String querystr = br.readLine();

		// the "title" arg specifies the default field to use
		// when no field is explicitly specified in the query.
		
		Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_35);
		//Analyzer analyzer = new KeywordAnalyzer();
		
		QueryParser queryParser = new QueryParser(Version.LUCENE_35, "title", analyzer);
		queryParser.setDefaultOperator(Operator.AND);
		Query q = queryParser.parse(querystr);
		
//		BooleanQuery bq = new BooleanQuery();
//		String[] splitted = querystr.split(" ");
//		for (String part : splitted) {
//			part = part.trim();
//			if ( ! part.equals(" ") ) {
//				Term term = new Term("title", part);
//				bq.add(new TermQuery(term), Occur.MUST);
//			}
//		}
//		Query q = bq;
//		
//		Term term = new Term("title", querystr);
		
//		PhraseQuery phraseQuery = new PhraseQuery();
//		phraseQuery.add(term);
//		Query q = phraseQuery;
//		PhraseQuery phraseQuery = new PhraseQuery();
//		String[] splitted = querystr.split(" ");
//		for (String part : splitted) {
//			part = part.trim();
//			if ( ! part.equals(" ") ) {
//				Term term = new Term("title", part);
//				phraseQuery.add(term);
//			}
//		}
//		Query q = phraseQuery;
		
		//Query q = new WildcardQuery(term);
		
		// 3. search
		int hitsPerPage = 10;
		IndexReader reader = IndexReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		// 4. display results
		System.out.println("Found " + hits.length + " hits.");
		for(int i=0;i<hits.length;++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			System.out.println((i + 1) + ". " + d.get("title"));
		}

		// searcher can only be closed when there
		// is no need to access the documents any more. 
		searcher.close();
	}

	private static void addDoc(IndexWriter w, String value) throws IOException {
		Document doc = new Document();
		doc.add(new Field("title", value, Field.Store.YES, Field.Index.ANALYZED));
		w.addDocument(doc);
	}
	*/
}
