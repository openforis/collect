/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeDefinitionFormValidator extends NodeDefinitionFormValidator {
	
	protected static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	protected static final String KEY_FIELD = "key";

	protected static final String KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY = "survey.validation.attribute.key_attribute_cannot_be_multiple";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateAttributeDefaults(ctx);
		validateMultipleAndKey(ctx);
	}
	
	private void validateMultipleAndKey(ValidationContext ctx) {
		Boolean multiple = getValue(ctx, MULTIPLE_FIELD);
		Boolean key = getValue(ctx, KEY_FIELD, false);
		if ( key != null && key && multiple ) {
			addInvalidMessage(ctx, KEY_FIELD, Labels.getLabel(KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY));
			addInvalidMessage(ctx, MULTIPLE_FIELD, Labels.getLabel(KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY));
		}
	}

	protected void validateAttributeDefaults(ValidationContext ctx) {
		//List<AttributeDefault> attributeDefaults = (List<AttributeDefault>) getValueFromForm(ctx, ATTRIBUTE_DEFAULTS_FIELD);
		//TODO
	}

}
