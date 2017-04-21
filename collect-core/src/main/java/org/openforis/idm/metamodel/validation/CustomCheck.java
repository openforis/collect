/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class CustomCheck extends Check<Attribute<?,?>> {

	private static final long serialVersionUID = 1L;

	private String expression;

	public CustomCheck() {
	}
	
	public CustomCheck(String expression) {
		this.expression = expression;
	}

	@Override
	public ValidationResultFlag evaluate(Attribute<?,?> node) {
		try {
			ExpressionEvaluator expressionEvaluator = node.getSurvey().getContext().getExpressionEvaluator();
			boolean valid = expressionEvaluator.evaluateBoolean(node.getParent(), node, getExpression());
			return ValidationResultFlag.valueOf(valid, this.getFlag());
		} catch (InvalidExpressionException e) {
			throw new IdmInterpretationError("Error evaluating custom check", e);
		}
	}
	
	@Override
	public String getExpression() {
		return this.expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	@Override
	protected String buildExpression() {
		return null;
	}
	
	@Override
	public String toString() {
		return "CUSTOM - " + super.toString();
	}

	
}
