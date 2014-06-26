package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CalculatedAttributeDefinitionFormValidator extends AttributeDefinitionFormValidator {
	
	private static final String FORMULAS_FIELD = "formulas";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateRequired(ctx, FORMULAS_FIELD);
	}

}
