/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeDefinitionFormValidator extends NodeDefinitionFormValidator {
	
	protected static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateAttributeDefaults(ctx);
	}
	
	protected void validateAttributeDefaults(ValidationContext ctx) {
		//List<AttributeDefault> attributeDefaults = (List<AttributeDefault>) getValueFromForm(ctx, ATTRIBUTE_DEFAULTS_FIELD);
		//TODO
	}

}
