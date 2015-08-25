package org.openforis.collect.datacleansing.manager;

import java.util.List;
import java.util.UUID;

import org.apache.commons.beanutils.PropertyUtils;
import org.openforis.collect.datacleansing.DataCleansingMetadata;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingManager {
	
	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private DataErrorQueryManager dataErrorQueryManager;
	@Autowired
	private DataErrorTypeManager dataErrorTypeManager;
	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	@Autowired
	private DataCleansingChainManager dataCleansingChainManager;

	public DataCleansingMetadata loadMetadata(CollectSurvey survey) {
		DataCleansingMetadata metadata = new DataCleansingMetadata(
				survey,
				dataQueryManager.loadBySurvey(survey), 
				dataErrorTypeManager.loadBySurvey(survey), 
				dataErrorQueryManager.loadBySurvey(survey), 
				dataCleansingStepManager.loadBySurvey(survey), 
				dataCleansingChainManager.loadBySurvey(survey));
		return metadata;
	}
	
	public void saveMetadata(CollectSurvey survey, DataCleansingMetadata metadata) {
		
		
		
	}
	
	private <T extends PersistedSurveyObject> void saveSurveyObjects(AbstractSurveyObjectManager<T, ?> manager, CollectSurvey survey, List<T> items) {
		List<T> existingItems = manager.loadBySurvey(survey);
		for (T item : items) {
			T existingItem = findItem(existingItems, item.getUuid());
			if (existingItem == null) {
				manager.save(item);
			} else {
				
			}
		}
		
	}
	
	private <T extends PersistedSurveyObject> T findItem(List<T> objects, UUID uuid) {
		for (T item : objects) {
			if (item.getUuid().equals(uuid)) {
				return item;
			}
		}
		return null;
	}
	
}
