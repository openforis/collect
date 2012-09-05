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
	public void validate(ValidationContext ctx) {
		super.validate(ctx);
		validateAttributeDefaults(ctx);
	}
	
	protected void validateAttributeDefaults(ValidationContext ctx) {
		//List<AttributeDefault> attributeDefaults = (List<AttributeDefault>) getValueFromForm(ctx, ATTRIBUTE_DEFAULTS_FIELD);
		//TODO
	}

}
