/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.ValidationContext;

/**
 * @author S. Ricci
 *
 */
public class CustomCheckFormValidator extends CheckFormValidator {

	protected static final String EXPRESSION_FIELD = "expression";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		if ( validateRequired(ctx, EXPRESSION_FIELD) ) {
			NodeDefinition parentDefn = getContextNode(ctx);
			validateBooleanExpressionField(ctx, parentDefn, EXPRESSION_FIELD);
		}
	}
	
}
