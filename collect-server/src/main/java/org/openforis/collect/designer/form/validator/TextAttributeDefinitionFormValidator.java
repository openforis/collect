package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class TextAttributeDefinitionFormValidator extends AttributeDefinitionFormValidator {
	
	protected static final String AUTOCOMPLETE_GROUP_FIELD = "autocompleteGroup";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateInternalName(ctx, AUTOCOMPLETE_GROUP_FIELD);
	}

}
