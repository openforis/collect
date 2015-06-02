/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.openforis.commons.lang.Strings;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResult;
import org.zkoss.bind.ValidationContext;

/**
 * @author S. Ricci
 *
 */
public class UniquenessCheckFormValidator extends CheckFormValidator {

	protected static final String EXPRESSION_FIELD = "expression";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateExpression(ctx);
	}
	
	private boolean validateExpression(ValidationContext ctx) {
		if ( validateRequired(ctx, EXPRESSION_FIELD) ) {
			ExpressionValidator expressionValidator = getExpressionValidator(ctx);
			NodeDefinition contextNode = getContextNode(ctx);
			String expression = getValue(ctx, EXPRESSION_FIELD);
			ExpressionValidationResult result = expressionValidator.validateUniquenessExpression(contextNode.getParentDefinition(), contextNode, expression);
			if ( result.isError() ) {
				String message = Strings.firstNotBlank(result.getDetailedMessage(), result.getMessage());
				addInvalidMessage(ctx, EXPRESSION_FIELD, getMessage(INVALID_EXPRESSION_MESSAGE_KEY, message));
				return false;
			}
		}
		return true;
	}
}
