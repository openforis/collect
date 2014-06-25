package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CalculatedAttributeDefinitionFormValidator extends AttributeDefinitionFormValidator {
	
	protected static final String FORMULA_FIELD = "formula";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		
		if ( validateRequired(ctx, FORMULA_FIELD) ) {
			validateValueExpression(ctx, getEditedNode(ctx), FORMULA_FIELD);
		}
	}

}
