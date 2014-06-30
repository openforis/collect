package org.openforis.collect.remoting.service.recordindex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.RecordIndexException;
import org.openforis.collect.manager.RecordIndexManager;
import org.openforis.collect.manager.RecordIndexManager.SearchType;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.RecordIndexProcess;
import org.openforis.collect.utils.ExecutorServiceUtil;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordIndexService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Autowired
	@Qualifier("persistedRecordIndexManager")
	private transient RecordIndexManager persistedIndexManager;
	
	@Autowired
	@Qualifier("volatileRecordIndexManager")
	private transient VolatileRecordIndexManager volatileIndexManager;
	
	private transient RecordIndexProcess indexProcess;
	
	public List<String> search(SearchType searchType, Survey survey, int attributeDefnId, int fieldIndex, String queryText, int maxResults)  throws RecordIndexException {
		Schema schema = survey.getSchema();
		AttributeDefinition defn = (AttributeDefinition) schema.getDefinitionById(attributeDefnId);
		String indexName = defn.getAnnotation(Annotation.AUTOCOMPLETE.getQName());
		if ( StringUtils.isNotBlank(indexName) ) {
			try {
				//search in ram directory
				List<String> tempResult = volatileIndexManager.search(searchType, survey, attributeDefnId, fieldIndex, queryText, maxResults);
				//search in file system index
				List<String> committedResult = persistedIndexManager.search(searchType, survey, attributeDefnId, fieldIndex, queryText, maxResults);
				List<String> result = mergeSearchResults(maxResults, tempResult, committedResult);
				return result;
			} catch(Exception e) {
				throw new RecordIndexException(e);
			}
		} else {
			throw new RecordIndexException("Index name is not defined for attribute with id: " + attributeDefnId);
		}
	}

	public void temporaryIndex(CollectRecord record) throws RecordIndexException {
		volatileIndexManager.index(record);
	}
	
	public void permanentlyIndex(CollectRecord record) {
		if ( indexProcess != null && indexProcess.isRunning() ) {
			indexProcess.cancel();
		}
		indexProcess = new RecordIndexProcess(persistedIndexManager, record);
		ExecutorServiceUtil.executeInCachedPool(indexProcess);
	}

	public boolean hasIndexableNodes(EntityDefinition rootEntityDefn) {
		return volatileIndexManager.hasIndexableNodes(rootEntityDefn);
	}
	
	protected List<String> mergeSearchResults(int maxResults, Collection<String> tempResult, Collection<String> committedResult) {
		Set<String> result = new HashSet<String>();
		result.addAll(tempResult);
		result.addAll(committedResult);
		List<String> sortedList = sortResults(result);
		if ( sortedList.size() > maxResults ) {
			sortedList = sortedList.subList(0, maxResults - 1);
		}
		return sortedList;
	}

	protected List<String> sortResults(Collection<String> result) {
		List<String> sortedList = new ArrayList<String>(result);
		Collections.sort(sortedList);
		return sortedList;
	}

	public void cleanTemporaryIndex() throws RecordIndexException {
		volatileIndexManager.cleanIndex();
	}

	public boolean isInited() {
		return persistedIndexManager.isInited();
	}

}
