package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListItemFormValidator extends SurveyObjectFormValidator<CodeListItem> {
	
	protected static final String CODE_FIELD = "code";
	
	public static final String CODE_ALREADY_DEFINED_MESSAGE_KEY = "survey.code_list.validation.code_already_defined";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateCode(ctx);
	}

	protected boolean validateCode(ValidationContext ctx) {
		boolean result = validateRequired(ctx, CODE_FIELD);
		if ( result ) {
			result = validateCodeUniqueness(ctx);
		}
		return result;
	}

	protected boolean validateCodeUniqueness(ValidationContext ctx) {
		SurveyObjectBaseVM<CodeListItem> viewModel = getSurveyObjectVM(ctx);
		CodeListItem editedItem = viewModel.getEditedItem();
		String code = (String) getValue(ctx, CODE_FIELD);
		CodeListItem parentItem = editedItem.getParentItem();
		CodeListItem existingItem;
		if ( parentItem != null ) {
			existingItem = parentItem.findChildItem(code);
		} else {
			CodeList codeList = editedItem.getCodeList();
			existingItem = codeList.findItem(code);
		}
		if ( existingItem != null && existingItem.getId() != editedItem.getId() ) {
			String message = Labels.getLabel(CODE_ALREADY_DEFINED_MESSAGE_KEY);
			addInvalidMessage(ctx, CODE_FIELD, message);
			return false;
		} else {
			return true;
		}
	}
	
}
