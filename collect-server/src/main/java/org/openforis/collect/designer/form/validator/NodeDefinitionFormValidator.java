package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDefinitionFormValidator extends FormValidator {

	protected static final String DESCRIPTION_FIELD = "description";
	protected static final String NAME_FIELD = "name";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
		validateDescription(ctx);
	}

	protected void validateName(ValidationContext ctx) {
		validateRequired(ctx, NAME_FIELD);
	}
	
	protected void validateDescription(ValidationContext ctx) {
		//TODO
		//Object value = getValue(ctx, DESCRIPTION_FIELD);
	}
	
}
