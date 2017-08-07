package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListFormValidator extends SurveyObjectFormValidator<CodeList> {
	
	protected static final String NAME_FIELD = "name";
	protected static final String[] RESERVED_NAMES = new String[] {CollectSurvey.SAMPLING_DESIGN_CODE_LIST_NAME};
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
	}

	protected boolean validateName(ValidationContext ctx) {
		boolean result = validateRequired(ctx, NAME_FIELD);
		if ( result ) {
			result = validateInternalName(ctx, NAME_FIELD);
			if ( result ) {
				result = validateNameNotReserved(ctx, NAME_FIELD, RESERVED_NAMES);
				if ( result ) {
					result = validateNameUniqueness(ctx);
				}
			}
		}
		return result;
	}

	protected boolean validateNameUniqueness(ValidationContext ctx) {
		SurveyObjectBaseVM<CodeList> viewModel = getVM(ctx);
		CodeList editedItem = viewModel.getEditedItem();
		CollectSurvey survey = viewModel.getSurvey();
		String name = (String) getValue(ctx, NAME_FIELD);
		CodeList existingItem = survey.getCodeList(name);
		if ( existingItem != null && existingItem.getId() != editedItem.getId() ) {
			String message = Labels.getLabel(ITEM_NAME_ALREADY_DEFINED_MESSAGE_KEY);
			addInvalidMessage(ctx, NAME_FIELD, message);
			return false;
		} else {
			return true;
		}
	}
	
}
