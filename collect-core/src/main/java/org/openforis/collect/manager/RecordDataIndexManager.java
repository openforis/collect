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
	
	protected RAMDirectory ramDirectory;
	
	@Autowired
	private ConfigurationManager configurationManager;

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
			try {
				IndexWriter indexWriter = createTemporaryIndexWriter();
				indexWriter.deleteAll();
				index(record, indexWriter);
			} catch (Exception e) {
				throw new RecordDataIndexException(e);
			}
		}
	}
	
	public void index(CollectRecord record) throws RecordDataIndexException {
		IndexWriter indexWriter = createIndexWriter();
		index(record, indexWriter);
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
	
	private void index(CollectRecord record, final IndexWriter indexWriter) throws RecordDataIndexException {
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
		} finally {
			closeIndexHandler(indexWriter);
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

	private List<String> mergeSearchResults(int maxResults, Set<String> tempResult, Set<String> committedResult) {
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

	private Set<String> search(String indexName, IndexSearcher indexSearcher, SearchType searchType, String queryText, int fieldIndex, int maxResults)
			throws Exception {
		Set<String> result = new HashSet<String>();
		String indexFieldKey = indexName + "_" +Integer.toString(fieldIndex);
		Query query = createQuery(searchType, indexFieldKey, queryText);
		TopDocs hits = indexSearcher.search(query, maxResults);
		ScoreDoc[] scoreDocs = hits.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
		    Document doc = indexSearcher.doc(scoreDoc.doc);
		    String value = doc.get(indexFieldKey);
			result.add(value);
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
		        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		        return indexSearcher;
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
					Document doc = new Document();
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
				String docFieldKey = indexName + "_" + Integer.toString(fieldIndex);
				Field docField = new Field(docFieldKey, fieldValueStr, Field.Store.YES, Field.Index.ANALYZED);
				doc.add(docField);
			}
		}
	}
	
	protected Query createQuery(SearchType searchType, String indexFieldKey, String searchText) throws ParseException {
		String escapedSearchText = QueryParser.escape(searchText.toLowerCase());
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
	
}
