package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

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

	protected void validateCode(ValidationContext ctx) {
		validateRequired(ctx, CODE_FIELD);
	}

}
