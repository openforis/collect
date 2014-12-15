package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyMainInfoFormValidator extends FormValidator {
	
	protected static final String NAME_FIELD = "name";

	public SurveyMainInfoFormValidator() {
		blocking = true;
	}
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
	}

	protected boolean validateName(ValidationContext ctx) {
		String field = NAME_FIELD;
		if ( validateRequired(ctx, field) && validateInternalName(ctx, field) ) {
			return true;
		} else {
			return false;
		}
	}
	
}
