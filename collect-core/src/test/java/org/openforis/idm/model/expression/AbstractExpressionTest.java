package org.openforis.idm.model.expression;

import org.openforis.idm.AbstractTest;
import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public class AbstractExpressionTest extends AbstractTest {

	protected Object evaluateExpression(Node<?> context, Node<?> thisNode, String expr) throws InvalidExpressionException {
		ExpressionFactory expressionFactory = context.getRecord().getSurveyContext().getExpressionFactory();
		ValueExpression expression = expressionFactory.createValueExpression(expr);
		Object object = expression.evaluate(context, thisNode);
		return object;
	}

	protected Object evaluateExpression(Node<?> context, String expr) throws InvalidExpressionException {
		return evaluateExpression(context, null, expr);
	}

	protected Object evaluateExpression(String expr) throws InvalidExpressionException {
		return evaluateExpression(cluster, expr);
	}

	protected Object evaluateMultiple(Node<?> context, Node<?> thisNode, String expr) throws InvalidExpressionException {
		ExpressionFactory expressionFactory = context.getRecord().getSurveyContext().getExpressionFactory();
		ValueExpression expression = expressionFactory.createValueExpression(expr);
		Object object = expression.evaluateMultiple(context, thisNode);
		return object;
	}
	
	protected Object evaluateMultiple(Node<?> context, String expr) throws InvalidExpressionException {
		return evaluateMultiple(context, null, expr);
	}
	
	protected boolean evaluateBooleanExpression(Node<?> context, Node<?> thisNode, String expr) throws InvalidExpressionException{
		ExpressionFactory expressionFactory = context.getRecord().getSurveyContext().getExpressionFactory();
		BooleanExpression expression = expressionFactory.createBooleanExpression(expr);
		boolean b = expression.evaluate(context, thisNode);
		return b;
	}
	
}
