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
public class NumericAttributeDefinitionFormValidator extends
		AttributeDefinitionFormValidator {
	
	//private static final String PRECISIONS_FIELD = "precisions";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validatePrecisions(ctx);
	}

	protected void validatePrecisions(ValidationContext ctx) {
		//@SuppressWarnings("unchecked")
		//List<PrecisionFormObject> precisions = (List<PrecisionFormObject>) getValue(ctx, PRECISIONS_FIELD);
		//TODO
	}
	
}
