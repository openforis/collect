package org.openforis.collect.datacleansing.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingMetadata;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.datacleansing.DataQuery;
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
	
	private static final String ID_PROPERTY_NAME = "id";
	private static final String UUID_PROPERTY_NAME = "uuid";
	
	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private DataErrorQueryManager dataErrorQueryManager;
	@Autowired
	private DataErrorQueryGroupManager dataErrorQueryGroupManager;
	@Autowired
	private DataErrorTypeManager dataErrorTypeManager;
	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	@Autowired
	private DataCleansingChainManager dataCleansingChainManager;
	@Autowired
	private DataErrorReportManager dataErrorReportManager;
	
	@Override
	public DataCleansingMetadata loadMetadata(CollectSurvey survey) {
		List<DataQuery> dataQueries = dataQueryManager.loadBySurvey(survey);
		List<DataErrorType> dataErrorTypes = dataErrorTypeManager.loadBySurvey(survey);
		List<DataErrorQuery> dataErrorQueries = dataErrorQueryManager.loadBySurvey(survey);
		for (DataErrorQuery dataErrorQuery : dataErrorQueries) {
			dataErrorQuery.setQuery(CollectionUtils.findItem(dataQueries, dataErrorQuery.getQueryId()));
			dataErrorQuery.setType(CollectionUtils.findItem(dataErrorTypes, dataErrorQuery.getTypeId()));
		}
		List<DataErrorQueryGroup> dataErrorQueryGroups = dataErrorQueryGroupManager.loadBySurvey(survey);
		for (DataErrorQueryGroup group : dataErrorQueryGroups) {
			List<DataErrorQuery> queries = group.getQueries();
			List<DataErrorQuery> correctQueries = new ArrayList<DataErrorQuery>(queries.size());
			for (DataErrorQuery dataErrorQuery : queries) {
				correctQueries.add(CollectionUtils.findItem(dataErrorQueries, dataErrorQuery.getId()));
			}
			group.removeAllQueries();
			group.allAllQueries(correctQueries);
		}
		List<DataCleansingStep> cleansingSteps = dataCleansingStepManager.loadBySurvey(survey);
		for (DataCleansingStep step : cleansingSteps) {
			step.setQuery(CollectionUtils.findItem(dataQueries, step.getQueryId()));
		}
		List<DataCleansingChain> cleansingChains = dataCleansingChainManager.loadBySurvey(survey);
		for (DataCleansingChain chain : cleansingChains) {
			List<DataCleansingStep> steps = chain.getSteps();
			List<DataCleansingStep> correctSteps = new ArrayList<DataCleansingStep>(steps.size());
			for (DataCleansingStep step : steps) {
				correctSteps.add(CollectionUtils.findItem(cleansingSteps, step.getId()));
			}
			chain.removeAllSteps();
			chain.addAllSteps(correctSteps);
		}
		
		DataCleansingMetadata metadata = new DataCleansingMetadata(
				survey,
				dataQueries, 
				dataErrorTypes, 
				dataErrorQueries, 
				dataErrorQueryGroups,
				cleansingSteps, 
				cleansingChains);
		return metadata;
	}
	
	@Transactional
	@Override
	public void saveMetadata(CollectSurvey survey, DataCleansingMetadata metadata) {
		saveItems(dataQueryManager, survey, metadata.getDataQueries());
		saveItems(dataErrorTypeManager, survey, metadata.getDataErrorTypes());
		saveItems(dataErrorQueryManager, survey, metadata.getDataErrorQueries());
		saveItems(dataErrorQueryGroupManager, survey, metadata.getDataErrorQueryGroups());
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
		List<AbstractSurveyObjectManager<?, ?>> managers = Arrays.<AbstractSurveyObjectManager<?, ?>>asList(
				dataCleansingChainManager, dataCleansingStepManager, 
				dataErrorReportManager, dataErrorQueryGroupManager, dataErrorQueryManager,
				dataErrorTypeManager, dataQueryManager);
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
			T oldItem = CollectionUtils.findItem(oldItems, item.getUuid(), UUID_PROPERTY_NAME);
			if (oldItem == null) {
				//new item
				item.setId(null);
				manager.save(item);
			} else {
				BeanUtils.copyProperties(item, oldItem, ID_PROPERTY_NAME, UUID_PROPERTY_NAME);
				manager.save(oldItem);
				item.setId(oldItem.getId());
			}
		}
		//delete removed items
		for (T oldItem : oldItems) {
			T newItem = CollectionUtils.findItem(items, oldItem.getUuid(), UUID_PROPERTY_NAME);
			if (newItem == null) {
				manager.delete(oldItem);
			}
		}
	}
	
}
