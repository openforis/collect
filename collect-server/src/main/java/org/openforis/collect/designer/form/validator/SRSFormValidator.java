package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class SRSFormValidator extends FormValidator {
	
	protected static final String ID_FIELD = "id";
	
	private static final String ITEM_ID_ALREADY_DEFINED_MESSAGE_KEY = "survey.srs.validation.id_already_defined";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateId(ctx);
	}

	protected boolean validateId(ValidationContext ctx) {
		boolean result = validateRequired(ctx, ID_FIELD);
		if ( result ) {
			result = validateIdUniqueness(ctx);
		}
		return result;
	}

	protected boolean validateIdUniqueness(ValidationContext ctx) {
		SurveyObjectBaseVM<SpatialReferenceSystem> viewModel = getSurveyItemEditVM(ctx);
		SpatialReferenceSystem editedItem = viewModel.getEditedItem();
		CollectSurvey survey = viewModel.getSurvey();
		String id = (String) getValue(ctx, ID_FIELD);
		SpatialReferenceSystem existingItem = survey.getSpatialReferenceSystem(id);
		if ( existingItem != null && ! existingItem.equals(editedItem) ) {
			String message = Labels.getLabel(ITEM_ID_ALREADY_DEFINED_MESSAGE_KEY);
			addInvalidMessage(ctx, ID_FIELD, message);
			return false;
		} else {
			return true;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected SurveyObjectBaseVM<SpatialReferenceSystem> getSurveyItemEditVM(ValidationContext ctx) {
		Object vm = getVM(ctx);
		if ( vm instanceof SurveyObjectBaseVM ) {
			return (SurveyObjectBaseVM<SpatialReferenceSystem>) vm;
		} else {
			throw new  IllegalStateException("Unexpected view model class: " + vm.getClass().getName());
		}
	}
}
