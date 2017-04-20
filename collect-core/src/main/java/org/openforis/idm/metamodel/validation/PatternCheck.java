/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.path.Path;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class PatternCheck extends Check<Attribute<?,?>> {

	private static final long serialVersionUID = 1L;

	private String regularExpression;

	@Override
	public String buildExpression() {
		StringBuilder sb = new StringBuilder();
		sb.append(ExpressionFactory.REGEX_PREFIX);
		sb.append(':');
		sb.append("test");
		sb.append('(');
		sb.append(Path.THIS_VARIABLE);
		sb.append(',');
		sb.append('\'');
		sb.append(regularExpression);
		sb.append('\'');
		sb.append(')');
		return sb.toString();
	}
	
	@Override
	public ValidationResultFlag evaluate(Attribute<?,?> node) {
		ExpressionEvaluator expressionEvaluator = node.getSurveyContext().getExpressionEvaluator();
		try {
			boolean result = expressionEvaluator.evaluateBoolean(node, node, getExpression());
			return ValidationResultFlag.valueOf(result, this.getFlag());
		} catch (InvalidExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getRegularExpression() {
		return this.regularExpression;
	}

	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
		resetExpression();
	}

	@Override
	public String toString() {
		return "PATTERN - " + super.toString();
	}
}
