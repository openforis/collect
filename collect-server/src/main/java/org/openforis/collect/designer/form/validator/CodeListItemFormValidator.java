package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.collect.manager.CodeListManager;
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
	
	public static final String CODE_ALREADY_DEFINED_MESSAGE_KEY = "survey.code_list.validation.code_already_defined";

	protected static final String PARENT_ITEM_ARG = "parentItem";
	protected static final String CODE_LIST_MANAGER_ARG = "codeListManager";

	protected static final String CODE_FIELD = "code";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateCode(ctx);
	}

	protected CodeListItem getParentItem(ValidationContext ctx) {
		CodeListItem result = (CodeListItem) ctx.getValidatorArg(PARENT_ITEM_ARG);
		return result;
	}
	
	protected CodeListManager getCodeListManager(ValidationContext ctx) {
		CodeListManager result = (CodeListManager) ctx.getValidatorArg(CODE_LIST_MANAGER_ARG);
		return result;
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
		CodeListItem existingItem = getExistingCodeListItem(ctx, code);
		if ( existingItem != null && existingItem.getId() != editedItem.getId() ) {
			String message = Labels.getLabel(CODE_ALREADY_DEFINED_MESSAGE_KEY);
			addInvalidMessage(ctx, CODE_FIELD, message);
			return false;
		} else {
			return true;
		}
	}
	
	protected CodeListItem getExistingCodeListItem(ValidationContext ctx, String code) {
		SurveyObjectBaseVM<CodeListItem> viewModel = getSurveyObjectVM(ctx);
		CodeListItem editedItem = viewModel.getEditedItem();
		CodeList codeList = editedItem.getCodeList();
		CodeListItem parentItem = getParentItem(ctx);
		CodeListItem existingItem = null;
		CodeListManager codeListManager = getCodeListManager(ctx);
		if ( parentItem == null ) {
			existingItem = codeListManager.loadRootItem(codeList, code, null);
		} else {
			existingItem = codeListManager.loadChildItem(parentItem, code, null);
		}
		return existingItem;
	}

}
