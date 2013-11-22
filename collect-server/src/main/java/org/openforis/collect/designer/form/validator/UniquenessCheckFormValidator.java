/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

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
			NodeDefinition parentDefn = getContextNode(ctx);
			String expression = getValue(ctx, EXPRESSION_FIELD);
			if ( StringUtils.isNotBlank(expression) && ! expressionValidator.validateUniquenessExpression(parentDefn, expression)) {
				addInvalidMessage(ctx, EXPRESSION_FIELD, Labels.getLabel(INVALID_EXPRESSION_MESSAGE_KEY));
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
}
