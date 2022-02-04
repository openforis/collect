/**
 * 
 */
package org.openforis.collect.manager;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.apache.poi.util.IOUtils;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.model.RecordFilter;
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
public class RecordIndexManager extends BaseStorageManager {

	private static final long serialVersionUID = 1L;

	private static final String COLLECT_INDEX_DEFAULT_FOLDER = "collect_index";
	protected static final String RECORD_ID_FIELD = "_record_id";
	private static final Version LUCENE_VERSION = Version.LUCENE_36;

	protected static final Logger LOG = LogManager.getLogger(RecordIndexManager.class);

	public enum SearchType {
		EQUAL, STARTS_WITH, CONTAINS;
	}

	@Autowired
	private transient RecordManager recordManager;
	
	protected Directory indexDirectory;
	protected boolean initialized;
	protected boolean cancelled;
	
	public RecordIndexManager() {
		super(COLLECT_INDEX_DEFAULT_FOLDER);
	}
	
	public synchronized boolean init() throws RecordIndexException {
		unlock();
		initStorageDirectory(ConfigurationItem.RECORD_INDEX_PATH);
		initIndexDirectory();
		cancelled = false;
		initialized = true;
		return initialized;
	}
	
	protected void initIndexDirectory() throws RecordIndexException {
		indexDirectory = createIndexDirectory();
		prepareIndexDirectory();
	}

	protected Directory createIndexDirectory() throws RecordIndexException {
		try {
			Directory directory = new SimpleFSDirectory(storageDirectory);
			return directory;
		} catch (IOException e) {
			throw new RecordIndexException(e);
		}
	}
	
	/**
	 * Prepare the index Directory for the first usage
	 * 
	 * @throws RecordIndexException
	 */
	protected void prepareIndexDirectory() throws RecordIndexException {
		IndexWriter indexWriter = createIndexWriter();
		close(indexWriter);
	}

	public synchronized void unlock() throws RecordIndexException {
		if ( storageDirectory != null ) {
			try {
				Directory directory = new SimpleFSDirectory(storageDirectory);
				if ( IndexWriter.isLocked(directory) ) {
					IndexWriter.unlock(directory);
				}
			} catch(Exception e) {
				deleteIndexRootDirectory();
			}
		}
	}
	
	public void destroyIndex() {
		try {
			deleteIndexRootDirectory();
		} catch (RecordIndexException e) {
			LOG.error("Error destroying index", e);
		}
	}

	protected void deleteIndexRootDirectory() throws RecordIndexException {
		if ( storageDirectory != null ) {
			try {
				FileUtils.forceDelete(storageDirectory);
			} catch (IOException e1) {
				throw new RecordIndexException(e1);
			}
		}
	}
	
	public void cleanIndex() throws RecordIndexException {
		IndexWriter indexWriter = createIndexWriter();
		deleteAllItems(indexWriter);
	}
	
	protected void deleteAllItems(IndexWriter indexWriter) throws RecordIndexException {
		try {
			indexWriter.deleteAll();
		} catch (Exception e) {
			throw new RecordIndexException(e);
		} finally {
			close(indexWriter);
		}
	}

	public void index(CollectRecord record) throws RecordIndexException {
		cancelled = false;
		IndexWriter indexWriter = null;
		try {
			indexWriter = createIndexWriter();
			Integer recordId = record.getId();
			deleteDocuments(indexWriter, recordId);
			index(indexWriter, record);
			//TODO cancel indexing if "cancelled" becomes "true"
		} catch (Exception e) {
			throw new RecordIndexException(e);
		} finally {
			close(indexWriter);
		}
	}
	
	public void indexAllRecords(CollectSurvey survey, String rootEntity) throws RecordIndexException {
		cancelled = false;
		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(survey.getSchema().getRootEntityDefinition(rootEntity).getId());
		List<CollectRecordSummary> summaries = recordManager.loadSummaries(filter);
		IndexWriter indexWriter = null;
		try {
			indexWriter = createIndexWriter();
			for (CollectRecordSummary s : summaries) {
				if ( ! cancelled ) {
					Integer recordId = s.getId();
					deleteDocuments(indexWriter, recordId);
					CollectRecord record = recordManager.load(survey, recordId, s.getStep());
					index(indexWriter, record);
				} else {
					break;
				}
			}
		} catch (Exception e) {
			throw new RecordIndexException(e);
		} finally {
			close(indexWriter);
		}
	}

	public void cancelIndexing() {
		cancelled = true;
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
		Deque<NodeDefinition> stack = new LinkedList<NodeDefinition>();
		stack.push(entityDefn);
		while ( ! stack.isEmpty() ) {
			NodeDefinition defn = stack.pop();
			String indexName = defn.getAnnotation(Annotation.AUTOCOMPLETE.getQName());
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
	
	protected void index(final IndexWriter indexWriter, CollectRecord record) throws RecordIndexException {
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
			throw new RecordIndexException(e);
		}
	}

	public void index(List<CollectRecord> records) throws Exception {
		for (CollectRecord record : records) {
			index(record);
		}
	}
	
    public List<String> search(SearchType searchType, Survey survey, int attributeDefnId, int fieldIndex, String queryText, int maxResults)  throws RecordIndexException {
    	Schema schema = survey.getSchema();
    	AttributeDefinition defn = (AttributeDefinition) schema.getDefinitionById(attributeDefnId);
    	String indexName = defn.getAnnotation(Annotation.AUTOCOMPLETE.getQName());
		if ( StringUtils.isNotBlank(indexName) ) {
			IndexSearcher indexSearcher = null;
			try {
		        indexSearcher = createIndexSearcher();
		        Set<String> result = search(indexName, indexSearcher, searchType, queryText, fieldIndex, maxResults);
		        List<String> sortedList = getSortedList(result);
		        return sortedList;
	        } catch(Exception e) {
	        	throw new RecordIndexException(e);
	        } finally {
	        	close(indexSearcher);
	        }
		} else {
			throw new RecordIndexException("Index name is not defined for attribute with id: " + attributeDefnId);
		}
    }

	protected void deleteDocuments(IndexWriter indexWriter, CollectRecord record) throws RecordIndexException {
		Integer id = record.getId();
		deleteDocuments(indexWriter, id);
	}
	
	
	protected void deleteDocuments(IndexWriter indexWriter, int recordId)
			throws RecordIndexException {
		try {
			Term term = new Term(RECORD_ID_FIELD, Integer.toString(recordId));
			indexWriter.deleteDocuments(term);
		} catch (Exception e) {
			throw new RecordIndexException(e);
		}
	}

	protected List<String> getSortedList(Collection<String> result) {
		List<String> sortedList = new ArrayList<String>(result);
		Collections.sort(sortedList);
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
    
	protected IndexWriter createIndexWriter() throws RecordIndexException {
		try {
			SimpleAnalyzer analyzer = new SimpleAnalyzer(LUCENE_VERSION);
			IndexWriterConfig conf = new IndexWriterConfig(LUCENE_VERSION, analyzer);
			conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
			IndexWriter indexWriter = new IndexWriter(indexDirectory, conf);
			return indexWriter;
		} catch (IOException e) {
			throw new RecordIndexException(e.getMessage(), e);
		}
	}

	protected IndexSearcher createIndexSearcher() throws RecordIndexException {
		IndexReader indexReader = null;
		try {
	        indexReader = IndexReader.open(indexDirectory);
			int numDocs = indexReader.numDocs();
			if ( numDocs > 0 ) {
		        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		        return indexSearcher;
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new RecordIndexException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(indexReader);
		}
    }
	
	protected void index(IndexWriter indexWriter, Attribute<?, ?> attr) {
		AttributeDefinition defn = attr.getDefinition();
		String indexName = defn.getAnnotation(Annotation.AUTOCOMPLETE.getQName());
		if ( StringUtils.isNotBlank(indexName) ) {
			try {
				Object value = attr.getValue();
				if ( value != null ) {
					Document doc = new Document();
					Record record = attr.getRecord();
					Integer recordId = record.getId();
					if ( recordId  != null ) {
						Field recordKeyField = createRecordIdField(recordId);
						doc.add(recordKeyField);
					}
					for (org.openforis.idm.model.Field<?> field : attr.getFields()) {
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
	
	protected Field createRecordIdField(int recordId) {
		Field recordKeyField = new Field(RECORD_ID_FIELD, Integer.toString(recordId), Field.Store.YES, Field.Index.NOT_ANALYZED);
		return recordKeyField;
	}

	protected Query createQuery(SearchType searchType, String indexFieldKey, String searchText) throws ParseException {
		String escapedSearchText = QueryParser.escape(searchText.trim().toLowerCase(Locale.ENGLISH));
		String queryText = escapedSearchText;
		
		if ( StringUtils.isNotBlank(queryText) ) {
			switch ( searchType ) {
			case STARTS_WITH:
				queryText = escapedSearchText + "*";
				break;
			case CONTAINS:
				//queryText = "*" + escapedSearchText + "*"; TODO support CONTAINS query
				queryText = escapedSearchText + "*";
				break;
			default:
				queryText = escapedSearchText;
			}
		}
		SimpleAnalyzer analyzer = new SimpleAnalyzer(LUCENE_VERSION);
		QueryParser queryParser = new QueryParser(LUCENE_VERSION, indexFieldKey, analyzer);
		queryParser.setDefaultOperator(Operator.AND);
		Query query = queryParser.parse(queryText);
        return query;
    }
	
	protected void close(IndexSearcher searcher) {
		if ( searcher != null ) {
			try {
				searcher.close();
				close(searcher.getIndexReader());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	protected void close(Closeable closeable) {
		if ( closeable != null ) {
			try {
				closeable.close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	public boolean isInited() {
		return initialized;
	}

	public void printAllDocuments(IndexReader r, PrintStream out) throws IOException {
		int num = r.numDocs();
		for ( int i = 0; i < num; i++) {
			if ( ! r.isDeleted( i)) {
				Document d = r.document( i);
		        out.println( "d=" +d);
			}
		}
		r.close();
	}
}
