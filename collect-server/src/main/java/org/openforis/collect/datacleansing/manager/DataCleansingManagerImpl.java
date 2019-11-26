package org.openforis.collect.datacleansing.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingMetadata;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.datacleansing.DataQueryType;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
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
	private DataQueryGroupManager dataQueryGroupManager;
	@Autowired
	private DataQueryTypeManager dataTypeManager;
	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	@Autowired
	private DataCleansingChainManager dataCleansingChainManager;
	@Autowired
	private DataCleansingReportManager dataCleansingReportManager;
	@Autowired
	private DataReportManager dataReportManager;
	
	@Override
	public DataCleansingMetadata loadMetadata(CollectSurvey survey) {
		List<DataQuery> dataQueries = dataQueryManager.loadBySurvey(survey);
		List<DataQueryType> dataQueryTypes = dataTypeManager.loadBySurvey(survey);
		for (DataQuery dataQuery : dataQueries) {
			Integer typeId = dataQuery.getTypeId();
			dataQuery.setType(typeId == null ? null : CollectionUtils.findItem(dataQueryTypes, typeId));
		}
		List<DataQueryGroup> dataQueryGroups = dataQueryGroupManager.loadBySurvey(survey);
		for (DataQueryGroup group : dataQueryGroups) {
			List<DataQuery> queries = group.getQueries();
			List<DataQuery> correctQueries = new ArrayList<DataQuery>(queries.size());
			for (DataQuery dataQuery : queries) {
				correctQueries.add(CollectionUtils.findItem(dataQueries, dataQuery.getId()));
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
				dataQueryTypes, 
				dataQueries, 
				dataQueryGroups,
				cleansingSteps, 
				cleansingChains);
		return metadata;
	}
	
	@Transactional
	@Override
	public void saveMetadata(CollectSurvey survey, DataCleansingMetadata metadata, boolean skipErrors, User activeUser) {
		saveItems(dataTypeManager, survey, metadata.getDataQueryTypes(), skipErrors, activeUser);
		saveItems(dataQueryManager, survey, metadata.getDataQueries(), skipErrors, activeUser);
		saveItems(dataQueryGroupManager, survey, metadata.getDataQueryGroups(), skipErrors, activeUser);
		saveItems(dataCleansingStepManager, survey, metadata.getCleansingSteps(), skipErrors, activeUser);
		saveItems(dataCleansingChainManager, survey, metadata.getCleansingChains(), skipErrors, activeUser);
	}
	
	@Transactional
	@Override
	public void moveMetadata(CollectSurvey fromSurvey, CollectSurvey toSurvey, User activeUser) {
		DataCleansingMetadata temporaryMetadata = loadMetadata(fromSurvey);
		saveMetadata(toSurvey, temporaryMetadata, false, activeUser);
		deleteMetadata(fromSurvey);
	}
	
	@Transactional
	@Override
	public void deleteMetadata(CollectSurvey survey) {
		List<AbstractSurveyObjectManager<Integer, ?, ?>> managers = Arrays.<AbstractSurveyObjectManager<Integer, ?, ?>>asList(
				dataCleansingReportManager,
				dataCleansingChainManager, 
				dataCleansingStepManager, 
				dataReportManager, 
				dataQueryGroupManager,
				dataQueryManager, 
				dataTypeManager);
		for (AbstractSurveyObjectManager<Integer, ?, ?> manager : managers) {
			manager.deleteBySurvey(survey);
		}
	}
	
	@Transactional
	@Override
	public void duplicateMetadata(CollectSurvey fromSurvey,
			CollectSurvey toSurvey, User activeUser) {
		DataCleansingMetadata metadata = loadMetadata(fromSurvey);
		saveMetadata(toSurvey, metadata, false, activeUser);
	}
	
	private <T extends PersistedSurveyObject<Integer>> void saveItems(AbstractSurveyObjectManager<Integer, T, ?> manager, 
			CollectSurvey survey, List<T> items, boolean skipErrors, User activeUser) {
		List<T> oldItems = manager.loadBySurvey(survey);
		for (T item : items) {
			item.replaceSurvey(survey);
			T oldItem = CollectionUtils.findItem(oldItems, item.getUuid(), UUID_PROPERTY_NAME);
			if (oldItem == null) {
				//new item
				item.setId(null);
				manager.save(item, activeUser);
			} else {
				try {
					BeanUtils.copyProperties(item, oldItem, ID_PROPERTY_NAME, UUID_PROPERTY_NAME);
				} catch (Exception e) {
					if (! skipErrors) {
						throw new RuntimeException("Error saving data cleansing items", e);
					}
				}
				manager.save(oldItem, activeUser);
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
