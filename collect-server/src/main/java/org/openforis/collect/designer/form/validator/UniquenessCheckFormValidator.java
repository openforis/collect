/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * @author S. Ricci
 *
 */
public class UniquenessCheckFormValidator extends CheckFormValidator {

	protected static final String EXPRESSION_FIELD = "expression";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateRequired(ctx, EXPRESSION_FIELD);
	}
}
