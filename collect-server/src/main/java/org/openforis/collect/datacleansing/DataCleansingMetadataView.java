package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openforis.collect.datacleansing.form.DataCleansingChainForm;
import org.openforis.collect.datacleansing.form.DataCleansingItemForm;
import org.openforis.collect.datacleansing.form.DataCleansingStepForm;
import org.openforis.collect.datacleansing.form.DataQueryForm;
import org.openforis.collect.datacleansing.form.DataQueryGroupForm;
import org.openforis.collect.datacleansing.form.DataQueryTypeForm;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingMetadataView {
	
	private List<DataQueryTypeForm> dataQueryTypes;
	private List<DataQueryForm> dataQueries;
	private List<DataQueryGroupForm> dataQueryGroups;
	private List<DataCleansingStepForm> cleansingSteps;
	private List<DataCleansingChainForm> cleansingChains;
	
	public DataCleansingMetadataView() {
	}
	
	public static DataCleansingMetadataView fromMetadata(DataCleansingMetadata metadata) {
		DataCleansingMetadataView view = new DataCleansingMetadataView();
		view.dataQueryTypes = PersistedSurveyObjects.convert(metadata.getDataQueryTypes(), DataQueryTypeForm.class);
		view.dataQueries = PersistedSurveyObjects.convert(metadata.getDataQueries(), DataQueryForm.class);
		view.dataQueryGroups = PersistedSurveyObjects.convert(metadata.getDataQueryGroups(), DataQueryGroupForm.class);
		view.cleansingSteps = PersistedSurveyObjects.convert(metadata.getCleansingSteps(), DataCleansingStepForm.class);
		view.cleansingChains = PersistedSurveyObjects.convert(metadata.getCleansingChains(), DataCleansingChainForm.class);
		return view;
	}
	
	public DataCleansingMetadata toMetadata(CollectSurvey survey) {
		//convert view items to metadata items
		List<DataQueryType> queryTypes = PersistedSurveyObjects.toItems(survey, this.dataQueryTypes, DataQueryType.class);
		List<DataQuery> queries = PersistedSurveyObjects.toItems(survey, this.dataQueries, DataQuery.class);
		List<DataQueryGroup> errorQueryGroups = PersistedSurveyObjects.toItems(survey, this.dataQueryGroups, DataQueryGroup.class);
		List<DataCleansingStep> cleansingSteps = PersistedSurveyObjects.toItems(survey, this.cleansingSteps, DataCleansingStep.class);
		List<DataCleansingChain> cleansingChains = PersistedSurveyObjects.toItems(survey, this.cleansingChains, DataCleansingChain.class);

		//create maps to facilitate object searching
		Map<Integer, DataQueryType> queryTypeByOriginalId = PersistedSurveyObjects.createObjectFromIdMap(queryTypes);
		Map<Integer, DataQuery> queryByOriginalId = PersistedSurveyObjects.createObjectFromIdMap(queries);
		Map<Integer, DataCleansingStep> cleansingStepByOriginalId = PersistedSurveyObjects.createObjectFromIdMap(cleansingSteps);
		
		//replace objects with same instances
		for (DataQuery query : queries) {
			query.setType(queryTypeByOriginalId.get(query.getTypeId()));
		}
		for (DataQueryGroup queryGroup : errorQueryGroups) {
			List<Integer> originalQueryIds = queryGroup.getQueryIds();
			List<DataQuery> originalQueries = new ArrayList<DataQuery>(originalQueryIds.size());
			for (Integer originalQueryId : originalQueryIds) {
				originalQueries.add(queryByOriginalId.get(originalQueryId));
			}
			queryGroup.removeAllQueries();
			queryGroup.setQueries(originalQueries);
		}
		for (DataCleansingStep step : cleansingSteps) {
			DataQuery query = queryByOriginalId.get(step.getQueryId());
			step.setQuery(query);
		}

		for (DataCleansingChain chain : cleansingChains) {
			DataCleansingChainForm viewItem = CollectionUtils.findItem(this.cleansingChains, chain.getId());
			List<Integer> stepIds = viewItem.getStepIds();
			for (Integer stepId : stepIds) {
				DataCleansingStep step = cleansingStepByOriginalId.get(stepId);
				chain.addStep(step);
			}
		}		
		
		PersistedSurveyObjects.resetIds(queryTypes);
		PersistedSurveyObjects.resetIds(queries);
		PersistedSurveyObjects.resetIds(errorQueryGroups);
		PersistedSurveyObjects.resetIds(cleansingSteps);
		PersistedSurveyObjects.resetIds(cleansingChains);
		
		DataCleansingMetadata metadata = new DataCleansingMetadata(survey, 
				queryTypes, 
				queries, 
				errorQueryGroups,
				cleansingSteps,
				cleansingChains);
		return metadata;
	}
	
	private static class PersistedSurveyObjects {
		
		public static <F extends DataCleansingItemForm<T>, T extends DataCleansingItem> List<F> convert(List<T> items, Class<F> resultType) {
			List<F> result = new ArrayList<F>(items.size());
			for (T i : items) {
				try {
					F o = resultType.getDeclaredConstructor(i.getClass()).newInstance(i);
					result.add(o);
				} catch (Exception e) {
					String message = String.format("Error creating objects of type %s from type %s", resultType.getName(), i.getClass().getName());
					throw new RuntimeException(message, e);
				}
			}
			return result;
		}
		
		private static <T extends DataCleansingItem> Map<Integer, T> createObjectFromIdMap(Collection<T> items) {
			Map<Integer, T> result = new HashMap<Integer, T>(items.size());
			for (T item : items) {
				result.put(item.getId(), item);
			}
			return result;
		}
		
		private static <T extends DataCleansingItem> void resetIds(Collection<T> items) {
			for (T item : items) {
				item.setId(null);
			}
		}
		
		private static <I extends DataCleansingItem, F extends DataCleansingItemForm<I>> List<I> toItems(
				CollectSurvey survey, List<F> formItems, Class<I> itemType) {
			if (org.apache.commons.collections.CollectionUtils.isEmpty(formItems)) {
				return Collections.emptyList();
			}
			List<I> items = new ArrayList<I>(formItems.size());
			for (F form : formItems) {
				try {
					I item = itemType.getDeclaredConstructor(CollectSurvey.class, UUID.class).newInstance(survey, form.getUuid());
					form.copyTo(item, "uuid");
					items.add(item);
				} catch (Exception e) {
					throw new RuntimeException("Error creating items from form items", e);
				}
			}
			return items;
		}
	}
	
	public List<DataQueryTypeForm> getDataQueryTypes() {
		return dataQueryTypes;
	}

	public List<DataQueryForm> getDataQueries() {
		return dataQueries;
	}
	
	public List<DataQueryGroupForm> getDataQueryGroups() {
		return dataQueryGroups;
	}

	public List<DataCleansingStepForm> getCleansingSteps() {
		return cleansingSteps;
	}

	public List<DataCleansingChainForm> getCleansingChains() {
		return cleansingChains;
	}
	
}