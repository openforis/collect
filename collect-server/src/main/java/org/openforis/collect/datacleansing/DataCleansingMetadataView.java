package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;
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
		view.dataQueries = CollectionUtils.convert(metadata.getDataQueries(), DataQueryForm.class);
		view.dataErrorTypes = CollectionUtils.convert(metadata.getDataErrorTypes(), DataErrorTypeForm.class);
		view.dataErrorQueries = CollectionUtils.convert(metadata.getDataErrorQueries(), DataErrorQueryForm.class);
		view.cleansingSteps = CollectionUtils.convert(metadata.getCleansingSteps(), DataCleansingStepForm.class);
		view.cleansingChains = CollectionUtils.convert(metadata.getCleansingChains(), DataCleansingChainForm.class);
		return view;
	}
	
	public DataCleansingMetadata toMetadata(CollectSurvey survey) {
		DataCleansingMetadata metadata = new DataCleansingMetadata(survey, 
				toItems(survey, this.dataQueries, DataQuery.class), 
				toItems(survey, this.dataErrorTypes, DataErrorType.class), 
				toItems(survey, this.dataErrorQueries, DataErrorQuery.class),
				toItems(survey, this.cleansingSteps, DataCleansingStep.class),
				toItems(survey, this.cleansingChains, DataCleansingChain.class));
		return metadata;
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