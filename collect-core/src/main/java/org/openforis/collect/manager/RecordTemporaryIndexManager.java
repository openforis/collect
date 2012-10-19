package org.openforis.collect.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordTemporaryIndexManager extends RecordIndexManager {

	@Autowired
	private RecordIndexManager recordIndexManager;

	@Override
	protected Directory createIndexDirectory() throws RecordIndexException {
		RAMDirectory directory = new RAMDirectory();
		return directory;
	}

	@Override
	public void index(CollectRecord record) throws RecordIndexException {
		IndexWriter indexWriter = null;
		try {
			indexWriter = createIndexWriter();
			indexWriter.deleteAll(); //temporary index is relative only to one record
			index(indexWriter, record);
		} catch (Exception e) {
			throw new RecordIndexException(e);
		} finally {
			close(indexWriter);
		}
	}

	public List<String> search(SearchType searchType, Survey survey, int attributeDefnId, int fieldIndex, String queryText, int maxResults)  throws RecordIndexException {
		Schema schema = survey.getSchema();
		AttributeDefinition defn = (AttributeDefinition) schema.getDefinitionById(attributeDefnId);
		String indexName = defn.getAnnotation(UIOptions.Annotation.AUTOCOMPLETE.getQName());
		if ( StringUtils.isNotBlank(indexName) ) {
			IndexSearcher indexSearcher = null;
			try {
				//search in ram directory
				indexSearcher = createIndexSearcher();
				Set<String> tempResult = search(indexName, indexSearcher, searchType, queryText, fieldIndex, maxResults);
				//search in file system index
				List<String> committedResult = recordIndexManager.search(searchType, survey, attributeDefnId, fieldIndex, queryText, maxResults);
				List<String> result = mergeSearchResults(maxResults, tempResult, committedResult);
				return result;
			} catch(Exception e) {
				throw new RecordIndexException(e);
			} finally {
				close(indexSearcher);
			}
		} else {
			throw new RecordIndexException("Index name is not defined for attribute with id: " + attributeDefnId);
		}
	}

	protected Set<String> searchInTemporaryDirectory(SearchType searchType, String indexName,
			int fieldIndex, String queryText, int maxResults)
					throws RecordIndexException, Exception {
		IndexSearcher indexSearcher = createIndexSearcher();
		Set<String> tempResult = search(indexName, indexSearcher, searchType, queryText, fieldIndex, maxResults);
		return tempResult;
	}

	protected List<String> mergeSearchResults(int maxResults, Collection<String> tempResult, Collection<String> committedResult) {
		Set<String> result = new HashSet<String>();
		result.addAll(tempResult);
		result.addAll(committedResult);
		List<String> sortedList = getSortedList(result);
		if ( sortedList.size() > maxResults ) {
			sortedList = sortedList.subList(0, maxResults - 1);
		}
		return sortedList;
	}

	public void permanentlyIndex(CollectRecord activeRecord) throws RecordIndexException {
		recordIndexManager.index(activeRecord);
	}

}
