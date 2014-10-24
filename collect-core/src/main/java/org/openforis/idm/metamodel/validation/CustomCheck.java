/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.expression.BooleanExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CustomCheck extends Check<Attribute<?,?>> {

	private static final long serialVersionUID = 1L;

	private String expression;

	public CustomCheck() {
	}
	
	public CustomCheck(String expression) {
		this.expression = expression;
	}

	public String getExpression() {
		return this.expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	@Override
	public ValidationResultFlag evaluate(Attribute<?,?> node) {
		try {
			BooleanExpression checkExpression = createCheckExpression(node.getRecord().getSurveyContext());
			boolean valid = checkExpression.evaluate(node.getParent(), node);
			return ValidationResultFlag.valueOf(valid, this.getFlag());
		} catch (InvalidExpressionException e) {
			throw new IdmInterpretationError("Error evaluating custom check", e);
		}
	}
	
	protected BooleanExpression createCheckExpression(SurveyContext surveyContext) throws InvalidExpressionException {
		String expr = getExpression();
		ExpressionFactory expressionFactory = surveyContext.getExpressionFactory();
		return expressionFactory.createBooleanExpression(expr);
	}
	
}
