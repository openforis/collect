package org.openforis.idm.model.expression;

import java.util.List;

import org.openforis.idm.AbstractTest;
import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractExpressionTest extends AbstractTest {

	
	protected Object evaluateExpression(Node<?> context, Node<?> thisNode, String expr) throws InvalidExpressionException {
		return expressionEvaluator.evaluateValue(context, thisNode, expr);
	}

	protected Object evaluateExpression(Node<?> context, String expr) throws InvalidExpressionException {
		return evaluateExpression(context, null, expr);
	}

	protected Object evaluateExpression(String expr) throws InvalidExpressionException {
		return evaluateExpression(cluster, expr);
	}

	protected Object evaluateMultiple(Node<?> context, String expr) throws InvalidExpressionException {
		return evaluateMultiple(context, null, expr);
	}

	protected Object evaluateMultiple(Node<?> context, Node<?> thisNode, String expr) throws InvalidExpressionException {
		List<Node<?>> nodes = expressionEvaluator.evaluateNodes(context, thisNode, expr);
		return nodes;
	}
	
	protected boolean evaluateBooleanExpression(Node<?> context, Node<?> thisNode, String expr) throws InvalidExpressionException{
		return expressionEvaluator.evaluateBoolean(context, thisNode, expr);
	}
	
}
