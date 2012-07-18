/**
 * 
 */
package org.openforis.collect.manager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.Configuration;
import org.openforis.idm.metamodel.AttributeDefinition;
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

	private static Log LOG = LogFactory.getLog(RecordDataIndexManager.class);

	protected static final String INDEX_ROOT_PATH_CONFIGURATION_KEY = "index_path";
	protected static final QName INDEX_NAME_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/collect", "index");
	
	private Map<String, IndexWriter> indexWriters;
	private Map<String, IndexSearcher> indexSearchers; 
	
	@Autowired
	private ConfigurationManager configurationManager;

	private String indexRootPath;
	
	protected void init() throws Exception {
		indexSearchers = new HashMap<String, IndexSearcher>();
		indexWriters = new HashMap<String, IndexWriter>();
		
		Configuration configuration = configurationManager.getConfiguration();
		indexRootPath = configuration.get(INDEX_ROOT_PATH_CONFIGURATION_KEY);
	}
	
	protected IndexWriter getIndexWriter(String name) throws Exception {
		IndexWriter indexWriter = indexWriters.get(name);
		if ( indexWriter == null ) {
			indexWriter = createIndexWriter(name);
			indexWriters.put(name, indexWriter);
		}
		return indexWriter;
	}

	protected IndexSearcher getIndexSearcher(String name) throws Exception {
		IndexSearcher indexSearcher = indexSearchers.get(name);
		if ( indexSearcher == null ) {
			indexSearcher = createIndexSearcher(name);
			indexSearchers.put(name, indexSearcher);
		}
		return indexSearcher;
	}
	
	protected IndexWriter createIndexWriter(String name) throws IOException, CorruptIndexException, LockObtainFailedException, Exception {
		String indexDirPath = indexRootPath + File.separator + name;
		File indexDir = new File(indexDirPath);
		if ( indexDir.exists() || indexDir.mkdirs() ) {
			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_35, analyzer);
			indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
			Directory directory = new SimpleFSDirectory(indexDir);
			IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
			return indexWriter;
		} else {
			throw new Exception("Cannot access index directory: " + indexDirPath);
		}
	}

	protected IndexSearcher createIndexSearcher(String name) throws Exception {
        String indexDirPath = indexRootPath + File.separator + name;
		File indexDir = new File(indexDirPath);
        if ( indexDir.exists() || indexDir.mkdirs() ) {
			Directory directory = new SimpleFSDirectory(indexDir);
	        IndexReader indexReader = IndexReader.open(directory);
	        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
	        return indexSearcher;
        } else {
        	throw new Exception("Cannot access index directory: " + indexDirPath);
        }
    }

	protected Query getQuery(int fieldIndex, String queryText) throws ParseException {
        Term term = new Term(Integer.toString(fieldIndex), queryText+"*");
        Query  query = new WildcardQuery(term);
        //This is wild card query, to use Fuzzy Query use following
        /*
         Term term = new Term(key,queryText+"~");
        Query  query = new FuzzyQuery(term,0.4F); 0.4 is 40 % matching with queryText
         * */
        return query;
    }
	
	public void index(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				NodeDefinition defn = node.getDefinition();
				if (defn instanceof AttributeDefinition ) {
					index((Attribute<?, ?>) node);
				}
			}
		});
	}

	protected void index(Attribute<?, ?> attr) {
		AttributeDefinition defn = attr.getDefinition();
		String indexName = defn.getAnnotation(INDEX_NAME_ANNOTATION);
		if ( StringUtils.isNotBlank(indexName) ) {
			try {
				IndexWriter indexWriter = getIndexWriter(indexName);
				Object value = attr.getValue();
				if ( value != null ) {
					Document doc = new Document();
					int fieldCount = attr.getFieldCount();
					for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++ ) {
						org.openforis.idm.model.Field<?> field = attr.getField(fieldIndex);
						index(doc, field);
					}
					indexWriter.addDocument(doc);
					indexWriter.numDocs();
					indexWriter.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void index(Document doc, org.openforis.idm.model.Field<?> field) {
		int fieldIndex = field.getIndex();
		Object fieldValue = field.getValue();
		if ( fieldValue != null ) {
			String fieldValueStr = fieldValue.toString();
			if ( StringUtils.isNotBlank(fieldValueStr)) {
				Field luceneField = new Field(Integer.toString(fieldIndex), fieldValueStr, Field.Store.YES, Field.Index.ANALYZED);
				doc.add(luceneField);
			}
		}
	}
	
	public void index(List<CollectRecord> records) throws CorruptIndexException, IOException {
		for (CollectRecord record : records) {
			index(record);
		}
	}

    public Set<String> search(Survey survey, int nodeId, int fieldIndex, String queryText)  throws CorruptIndexException, IOException, ParseException {
    	Set<String> result = new HashSet<String>();
    	Schema schema = survey.getSchema();
    	NodeDefinition defn = schema.getById(nodeId);
    	String indexName = defn.getAnnotation(INDEX_NAME_ANNOTATION);
		if ( StringUtils.isNotBlank(indexName) ) {
			try {
		        IndexSearcher indexSearcher = getIndexSearcher(indexName);
		        String key = Integer.toString(nodeId);
				Query query = getQuery(fieldIndex, queryText);
		        TopDocs hits = indexSearcher.search(query, 10);
		        for(int i=0;i<hits.scoreDocs.length;i++) {
		            ScoreDoc scoreDoc = hits.scoreDocs[i];
		            Document doc = indexSearcher.doc(scoreDoc.doc);
		            String value = doc.get(key);
					result.add(value);
		        }
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
        return result;
    }

}
