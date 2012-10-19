package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeAttributeDefinitionFormValidator extends AttributeDefinitionFormValidator {
	
	protected static final String LIST_FIELD = "list";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateRequired(ctx, LIST_FIELD);
	}


}
