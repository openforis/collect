package org.openforis.collect.designer.form.validator;

import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CalculatedAttributeFormulaFormValidator extends FormValidator {
	
	private static final String PARENT_DEFINITION_ARG = "parentDefinition";

	protected static final String EXPRESSION_FIELD = "expression";
	protected static final String CONDITION_FIELD = "condition";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateExpression(ctx);
		validateCondition(ctx);
	}

	private void validateExpression(ValidationContext ctx) {
		NodeDefinition contextNode = getContextNode(ctx);
		if ( validateRequired(ctx, EXPRESSION_FIELD) ) {
			validateValueExpression(ctx, contextNode, EXPRESSION_FIELD);
		}
	}
	
	private void validateCondition(ValidationContext ctx) {
		NodeDefinition contextNode = getContextNode(ctx);
		validateBooleanExpression(ctx, contextNode, CONDITION_FIELD);
	}
	
	private NodeDefinition getContextNode(ValidationContext ctx) {
		NodeDefinition result = (NodeDefinition) ctx.getValidatorArg(PARENT_DEFINITION_ARG);
		return result;
	}

}
