package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class UnitFormValidator extends SurveyObjectFormValidator<Unit> {
	
	protected static final String NAME_FIELD = "name";
	protected static final String DIMENSION_FIELD_NAME = "dimensionLabel";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
		validateRequired(ctx, DIMENSION_FIELD_NAME);
	}

	protected boolean validateName(ValidationContext ctx) {
		boolean result = validateRequired(ctx, NAME_FIELD);
		if ( result ) {
			result = validateInternalName(ctx, NAME_FIELD);
			if ( result ) {
				result = validateNameUniqueness(ctx);
			}
		}
		return result;
	}

	protected boolean validateNameUniqueness(ValidationContext ctx) {
		SurveyObjectBaseVM<Unit> viewModel = getVM(ctx);
		Unit editedItem = viewModel.getEditedItem();
		CollectSurvey survey = viewModel.getSurvey();
		String name = (String) getValue(ctx, NAME_FIELD);
		Unit existingItem = survey.getUnit(name);
		if ( existingItem != null && existingItem.getId() != editedItem.getId() ) {
			String message = Labels.getLabel(ITEM_NAME_ALREADY_DEFINED_MESSAGE_KEY);
			addInvalidMessage(ctx, NAME_FIELD, message);
			return false;
		} else {
			return true;
		}
	}
	

}
