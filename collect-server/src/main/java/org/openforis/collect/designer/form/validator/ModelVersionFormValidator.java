package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class ModelVersionFormValidator extends SurveyObjectFormValidator<ModelVersion> {
	
	protected static final String NAME_FIELD = "name";
	protected static final String DATE_FIELD = "date";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateRequired(ctx, DATE_FIELD);
		validateName(ctx);
	}

	protected boolean validateName(ValidationContext ctx) {
		if ( validateRequired(ctx, NAME_FIELD) ) {
			return validateNameUniqueness(ctx);
		}
		return false;
	}

	protected boolean validateNameUniqueness(ValidationContext ctx) {
		SurveyObjectBaseVM<ModelVersion> viewModel = getSurveyObjectVM(ctx);
		ModelVersion editedItem = viewModel.getEditedItem();
		CollectSurvey survey = viewModel.getSurvey();
		String name = (String) getValue(ctx, NAME_FIELD);
		ModelVersion existingItem = survey.getVersion(name);
		if ( existingItem != null && existingItem.getId() != editedItem.getId() ) {
			String message = Labels.getLabel(ITEM_NAME_ALREADY_DEFINED_MESSAGE_KEY);
			addInvalidMessage(ctx, NAME_FIELD, message);
			return false;
		} else {
			return true;
		}
	}
	
}
