/**
 * 
 */
package org.openforis.idm.model.expression;

import java.util.List;

import org.openforis.idm.model.Node;
import org.openforis.idm.model.expression.internal.ModelJXPathCompiledExpression;
import org.openforis.idm.model.expression.internal.ModelJXPathContext;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class ModelPathExpression extends AbstractExpression {

	ModelPathExpression(ModelJXPathCompiledExpression expression, ModelJXPathContext context) {
		super(expression, context);
	}

	public Node<?> evaluate(Node<?> contextNode, Node<?> thisNode) throws InvalidExpressionException {
		List<Node<?>> list = iterate(contextNode, thisNode);
		if (list != null && list.size() == 1) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public List<Node<?>> iterate(Node<?> contextNode, Node<?> thisNode) throws InvalidExpressionException {
		List<Node<?>> list = evaluateMultiple(contextNode, thisNode);
		return list;
	}
}
