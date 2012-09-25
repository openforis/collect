package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class SRSFormValidator extends FormValidator {
	
	protected static final String ID_FIELD = "id";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateId(ctx);
	}

	protected void validateId(ValidationContext ctx) {
		validateRequired(ctx, ID_FIELD);
	}

}
