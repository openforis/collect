/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * @author S. Ricci
 *
 */
public class PatternCheckFormValidator extends CheckFormValidator {

	protected static final String REGULAR_EXPRESSION_FIELD = "regularExpression";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateRequired(ctx, REGULAR_EXPRESSION_FIELD);
	}
}
