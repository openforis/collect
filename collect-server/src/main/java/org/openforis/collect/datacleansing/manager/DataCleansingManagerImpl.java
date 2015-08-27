package org.openforis.collect.datacleansing.manager;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.openforis.collect.datacleansing.DataCleansingMetadata;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Transactional
public class DataCleansingManagerImpl implements DataCleansingMetadataManager {
	
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
	
	private List<AbstractSurveyObjectManager<?, ?>> managers;

	@PostConstruct
	private void init() {
		managers = Arrays.<AbstractSurveyObjectManager<?, ?>> asList(
				dataCleansingChainManager, dataCleansingStepManager,
				dataErrorQueryManager, dataErrorTypeManager, dataQueryManager);
	}
	
	@Override
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
	
	@Transactional
	@Override
	public void saveMetadata(CollectSurvey survey, DataCleansingMetadata metadata) {
		saveItems(dataQueryManager, survey, metadata.getDataQueries());
		saveItems(dataErrorTypeManager, survey, metadata.getDataErrorTypes());
		saveItems(dataErrorQueryManager, survey, metadata.getDataErrorQueries());
		saveItems(dataCleansingStepManager, survey, metadata.getCleansingSteps());
		saveItems(dataCleansingChainManager, survey, metadata.getCleansingChains());
	}
	
	@Transactional
	@Override
	public void moveMetadata(CollectSurvey fromSurvey, CollectSurvey toSurvey) {
		DataCleansingMetadata temporaryMetadata = loadMetadata(fromSurvey);
		saveMetadata(toSurvey, temporaryMetadata);
		deleteMetadata(fromSurvey);
	}
	
	@Transactional
	@Override
	public void deleteMetadata(CollectSurvey survey) {
		for (AbstractSurveyObjectManager<?, ?> manager : managers) {
			manager.deleteBySurvey(survey);
		}
	}
	
	@Transactional
	@Override
	public void duplicateMetadata(CollectSurvey fromSurvey,
			CollectSurvey toSurvey) {
		DataCleansingMetadata metadata = loadMetadata(fromSurvey);
		saveMetadata(toSurvey, metadata);
	}
	
	private <T extends PersistedSurveyObject> void saveItems(AbstractSurveyObjectManager<T, ?> manager, 
			CollectSurvey survey, List<T> items) {
		List<T> oldItems = manager.loadBySurvey(survey);
		for (T item : items) {
			item.replaceSurvey(survey);
			T oldItem = CollectionUtils.findItem(oldItems, item.getUuid(), "uuid");
			if (oldItem == null) {
				//new item
				item.setId(null);
				manager.save(item);
			} else {
				BeanUtils.copyProperties(item, oldItem, "id", "uuid");
				manager.save(oldItem);
				item.setId(oldItem.getId());
			}
		}
		//delete removed items
		for (T oldItem : oldItems) {
			T newItem = CollectionUtils.findItem(items, oldItem.getUuid(), "uuid");
			if (newItem == null) {
				manager.delete(oldItem);
			}
		}
	}
	
}
