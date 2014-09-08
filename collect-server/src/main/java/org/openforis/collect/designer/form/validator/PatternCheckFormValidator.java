/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * @author S. Ricci
 *
 */
public class PatternCheckFormValidator extends CheckFormValidator {

	protected static final String REGULAR_EXPRESSION_FIELD = "regularExpression";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateExpression(ctx);
	}
	
	private boolean validateExpression(ValidationContext ctx) {
		if ( validateRequired(ctx, REGULAR_EXPRESSION_FIELD) ) {
			ExpressionValidator expressionValidator = getExpressionValidator(ctx);
			String expression = getValue(ctx, REGULAR_EXPRESSION_FIELD);
			if ( StringUtils.isNotBlank(expression) && ! expressionValidator.validateRegularExpression(expression)) {
				addInvalidMessage(ctx, REGULAR_EXPRESSION_FIELD, Labels.getLabel(INVALID_EXPRESSION_MESSAGE_KEY));
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
}
