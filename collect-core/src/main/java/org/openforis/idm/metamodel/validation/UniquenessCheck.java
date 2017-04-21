/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.openforis.commons.collection.Predicate;
import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class UniquenessCheck extends Check<Attribute<?, ?>> {

	private static final long serialVersionUID = 1L;

	private String expression;

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
	public ValidationResultFlag evaluate(final Attribute<?, ?> attribute) {
		try {
			SurveyContext recordContext = attribute.getRecord().getSurveyContext();
			ExpressionEvaluator expressionEvaluator = recordContext.getExpressionEvaluator();
			Node<?> duplicateNode = expressionEvaluator.findNode(attribute.getParent(), attribute, expression, new Predicate<Node<?>>() {
				public boolean evaluate(Node<?> node) {
					if (node instanceof Attribute && node != attribute) {
						Value value = ((Attribute<?, ?>) node).getValue();
						if ( value != null && value.equals(attribute.getValue()) ) {
							return true;
						}
					}
					return false;
				};
			});
			boolean unique = duplicateNode == null;
			return ValidationResultFlag.valueOf(unique, this.getFlag());
		} catch (InvalidExpressionException e) {
			throw new IdmInterpretationError("Error evaluating uniqueness check", e);
		}
	}

	@Override
	public String toString() {
		return "UNIQUENESS - " + super.toString();
	}

}
