/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import java.util.List;

import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.ModelPathExpression;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class UniquenessCheck extends Check<Attribute<?, ?>> {

	private static final long serialVersionUID = 1L;

	private String expression;

	public String getExpression() {
		return this.expression;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	@Override
	public ValidationResultFlag evaluate(Attribute<?, ?> attribute) {
		try {
			SurveyContext recordContext = attribute.getRecord().getSurveyContext();
			ExpressionFactory expressionFactory = recordContext.getExpressionFactory();
			ModelPathExpression pathExpression = expressionFactory.createModelPathExpression(expression);
			List<Node<?>> list = pathExpression.iterate(attribute.getParent(), attribute);
			boolean unique = true;
			if (list != null && list.size() > 1) {
				for (Node<?> node : list) {
					if (node != attribute) {
						if (node instanceof Attribute) {
							Value value = ((Attribute<?, ?>) node).getValue();
							if ( value != null && value.equals(attribute.getValue()) ) {
								unique = false;
								break;
							}
						}
					}
				}
			}
			return ValidationResultFlag.valueOf(unique, this.getFlag());
		} catch (InvalidExpressionException e) {
			throw new IdmInterpretationError("Error evaluating uniqueness check", e);
		}
	}

}
