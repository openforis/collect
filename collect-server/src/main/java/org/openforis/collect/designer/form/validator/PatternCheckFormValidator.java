/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import org.openforis.commons.lang.Strings;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResult;
import org.zkoss.bind.ValidationContext;

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
		if (! validateRequired(ctx, REGULAR_EXPRESSION_FIELD) ) {
			return false;
		}
		ExpressionValidator expressionValidator = getExpressionValidator(ctx);
		String expression = getValue(ctx, REGULAR_EXPRESSION_FIELD);
		ExpressionValidationResult result = expressionValidator.validateRegularExpression(expression);
		if (result.isError()) {
			String message = Strings.firstNotBlank(result.getDetailedMessage(), result.getMessage());
			addInvalidMessage(ctx, REGULAR_EXPRESSION_FIELD, getMessage(INVALID_EXPRESSION_MESSAGE_KEY, message));
			return false;
		}
		return true;
	}
}
