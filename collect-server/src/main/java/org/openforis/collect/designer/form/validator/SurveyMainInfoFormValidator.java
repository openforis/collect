package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyMainInfoFormValidator extends FormValidator {
	
	protected static final String NAME_FIELD = "name";
	protected static final String URI_FIELD = "uri";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
		validateUri(ctx);
	}

	protected void validateName(ValidationContext ctx) {
		validateRequired(ctx, NAME_FIELD);
	}

	protected void validateUri(ValidationContext ctx) {
		super.validateUri(ctx, URI_FIELD);
	}
	
}
