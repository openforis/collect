package org.openforis.idm.model.expression;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.internal.ModelJXPathCompiledExpression;
import org.openforis.idm.model.expression.internal.ModelJXPathContext;

/**
 * @author G. Miceli
 */
public class AbsoluteModelPathExpression {

	private ModelPathExpression modelPathExpression;
	private String root;

	AbsoluteModelPathExpression(String root, ModelJXPathCompiledExpression expression, ModelJXPathContext context) {
		this.root = root;
		this.modelPathExpression = new ModelPathExpression(expression, context);
	}

	public AbsoluteModelPathExpression(String root) {
		this.root = root;
	}

	public Node<?> evaluate(Record record) throws InvalidExpressionException {
		List<Node<?>> list = iterate(record);
		if (list != null && list.size() == 1) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public List<Node<?>> iterate(Record record) throws InvalidExpressionException {
		Entity contextNode = record.getRootEntity();
		if ( contextNode.getName().equals(root) ) {
			if ( modelPathExpression == null ) {
				List<Node<?>> list = new ArrayList<Node<?>>();
				list.add(contextNode);
				return list;
			} else {
				List<Node<?>> list = modelPathExpression.evaluateMultiple(contextNode, contextNode);
				return list;
			}
		} else {
			throw new InvalidExpressionException(root+" is not a valid root");
		}
	}

}
