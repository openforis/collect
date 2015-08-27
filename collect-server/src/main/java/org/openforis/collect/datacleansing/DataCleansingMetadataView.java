package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openforis.collect.datacleansing.form.DataCleansingChainForm;
import org.openforis.collect.datacleansing.form.DataCleansingItemForm;
import org.openforis.collect.datacleansing.form.DataCleansingStepForm;
import org.openforis.collect.datacleansing.form.DataErrorQueryForm;
import org.openforis.collect.datacleansing.form.DataErrorTypeForm;
import org.openforis.collect.datacleansing.form.DataQueryForm;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingMetadataView {
	
	private List<DataQueryForm> dataQueries;
	private List<DataErrorTypeForm> dataErrorTypes;
	private List<DataErrorQueryForm> dataErrorQueries;
	private List<DataCleansingStepForm> cleansingSteps;
	private List<DataCleansingChainForm> cleansingChains;
	
	public DataCleansingMetadataView() {
	}
	
	public static DataCleansingMetadataView fromMetadata(DataCleansingMetadata metadata) {
		DataCleansingMetadataView view = new DataCleansingMetadataView();
		view.dataQueries = PersistedSurveyObjects.convert(metadata.getDataQueries(), DataQueryForm.class);
		view.dataErrorTypes = PersistedSurveyObjects.convert(metadata.getDataErrorTypes(), DataErrorTypeForm.class);
		view.dataErrorQueries = PersistedSurveyObjects.convert(metadata.getDataErrorQueries(), DataErrorQueryForm.class);
		view.cleansingSteps = PersistedSurveyObjects.convert(metadata.getCleansingSteps(), DataCleansingStepForm.class);
		view.cleansingChains = PersistedSurveyObjects.convert(metadata.getCleansingChains(), DataCleansingChainForm.class);
		return view;
	}
	
	public DataCleansingMetadata toMetadata(CollectSurvey survey) {
		//convert view items to metadata items
		List<DataQuery> queries = PersistedSurveyObjects.toItems(survey, this.dataQueries, DataQuery.class);
		List<DataErrorType> errorTypes = PersistedSurveyObjects.toItems(survey, this.dataErrorTypes, DataErrorType.class);
		List<DataErrorQuery> errorQueries = PersistedSurveyObjects.toItems(survey, this.dataErrorQueries, DataErrorQuery.class);
		List<DataCleansingStep> cleansingSteps = PersistedSurveyObjects.toItems(survey, this.cleansingSteps, DataCleansingStep.class);
		List<DataCleansingChain> cleansingChains = PersistedSurveyObjects.toItems(survey, this.cleansingChains, DataCleansingChain.class);

		//create maps to facilitate object searching
		Map<Integer, DataErrorType> errorTypeByOriginalId = PersistedSurveyObjects.createObjectFromIdMap(errorTypes);
		Map<Integer, DataQuery> queryByOriginalId = PersistedSurveyObjects.createObjectFromIdMap(queries);
		Map<Integer, DataCleansingStep> cleansingStepByOriginalId = PersistedSurveyObjects.createObjectFromIdMap(cleansingSteps);
		
		//replace objects with same instances
		for (DataErrorQuery errorQuery : errorQueries) {
			errorQuery.setQuery(queryByOriginalId.get(errorQuery.getQueryId()));
			errorQuery.setType(errorTypeByOriginalId.get(errorQuery.getTypeId()));
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
		
		PersistedSurveyObjects.resetIds(queries);
		PersistedSurveyObjects.resetIds(errorTypes);
		PersistedSurveyObjects.resetIds(errorQueries);
		PersistedSurveyObjects.resetIds(cleansingSteps);
		PersistedSurveyObjects.resetIds(cleansingChains);
		
		DataCleansingMetadata metadata = new DataCleansingMetadata(survey, 
				queries, 
				errorTypes, 
				errorQueries,
				cleansingSteps,
				cleansingChains);
		return metadata;
	}
	
	private static class PersistedSurveyObjects {
		
		public static <F extends DataCleansingItemForm<T>, T extends PersistedSurveyObject> List<F> convert(List<T> items, Class<F> resultType) {
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
		
		private static <T extends PersistedSurveyObject> Map<Integer, T> createObjectFromIdMap(Collection<T> items) {
			Map<Integer, T> result = new HashMap<Integer, T>(items.size());
			for (T item : items) {
				result.put(item.getId(), item);
			}
			return result;
		}
		
		private static <T extends PersistedSurveyObject> void resetIds(Collection<T> items) {
			for (T item : items) {
				item.setId(null);
			}
		}
		
		private static <I extends PersistedSurveyObject, F extends DataCleansingItemForm<I>> List<I> toItems(
				CollectSurvey survey, List<F> formItems, Class<I> itemType) {
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
	
	public List<DataQueryForm> getDataQueries() {
		return dataQueries;
	}

	public List<DataErrorTypeForm> getDataErrorTypes() {
		return dataErrorTypes;
	}

	public List<DataErrorQueryForm> getDataErrorQueries() {
		return dataErrorQueries;
	}

	public List<DataCleansingStepForm> getCleansingSteps() {
		return cleansingSteps;
	}

	public List<DataCleansingChainForm> getCleansingChains() {
		return cleansingChains;
	}
	
}