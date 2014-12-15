/**
 * 
 */
package org.openforis.idm.model.expression;

import org.openforis.idm.model.Node;
import org.openforis.idm.model.expression.internal.ModelJXPathCompiledExpression;
import org.openforis.idm.model.expression.internal.ModelJXPathContext;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class ValueExpression extends AbstractExpression {

	ValueExpression(ModelJXPathCompiledExpression expression, ModelJXPathContext context) {
		super(expression, context);
	}

	public Object evaluate(Node<?> contextNode, Node<?> thisNode) throws InvalidExpressionException {
		return evaluateSingle(contextNode, thisNode);
	}
}
