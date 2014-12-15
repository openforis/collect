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
 * @author S. Ricci
 * @author D. Wiell
 */
public class BooleanExpression extends AbstractExpression {

	public BooleanExpression(ModelJXPathCompiledExpression compiledExpression, ModelJXPathContext jxPathContext) {
		super(compiledExpression, jxPathContext);
	}

	public boolean evaluate(Node<?> contextNode, Node<?> thisNode) throws InvalidExpressionException {
		Object result = evaluateSingle(contextNode, thisNode);
		if (result == null) {
			return false;
		} else if (result instanceof Boolean) {
			return (Boolean) result;
		} else {
			// result is not null
			return true;
		}
	}
}
