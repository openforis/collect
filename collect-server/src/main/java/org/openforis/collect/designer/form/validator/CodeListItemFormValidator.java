package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyItemEditVM;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListItemFormValidator extends FormValidator {
	
	protected static final String CODE_FIELD = "code";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateCode(ctx);
	}

	protected boolean validateCode(ValidationContext ctx) {
		boolean result = validateRequired(ctx, CODE_FIELD);
//		if ( result ) {
//			
//		}
		return result;
	}

//	protected boolean validateNameUniqueness(ValidationContext ctx) {
//		SurveyItemEditVM<CodeListItem> viewModel = getSurveyItemEditVM(ctx);
//		CodeListItem editedItem = viewModel.getEditedItem();
//		CollectSurvey survey = viewModel.getSurvey();
//		String code = (String) getValue(ctx, CODE_FIELD);
//		CodeListItem parentItem = editedItem.getParentItem();
//		CodeListItem existingItem;
//		if ( parentItem != null ) {
//			existingItem = parentItem.getChildItem(code);
//		} else {
//			CodeList codeList = editedItem.getCodeList();
//			existingItem = codeList.getItem(code);
//		}
//		if ( existingItem != null && ! existingItem.getCode().equals(editedItem.getCode()) ) {
//			String message = Labels.getLabel(ITEM_NAME_ALREADY_DEFINED_MESSAGE_KEY);
//			addInvalidMessage(ctx, CODE_FIELD, message);
//			return false;
//		} else {
//			return true;
//		}
//	}
	
	protected SurveyItemEditVM<CodeListItem> getSurveyItemEditVM(ValidationContext ctx) {
		Object vm = getVM(ctx);
		if ( vm instanceof SurveyItemEditVM ) {
			@SuppressWarnings("unchecked")
			SurveyItemEditVM<CodeListItem> viewModel = (SurveyItemEditVM<CodeListItem>) vm;
			return viewModel;
		} else {
			throw new  IllegalStateException("Unexpected view model class: " + vm.getClass().getName());
		}
	}
}
