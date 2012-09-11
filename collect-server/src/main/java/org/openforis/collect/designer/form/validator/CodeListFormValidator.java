package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListFormValidator extends FormValidator {
	
	protected static final String NAME_FIELD = "name";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
	}

	protected void validateName(ValidationContext ctx) {
		validateRequired(ctx, NAME_FIELD);
	}

}
