package org.openforis.idm.model.expression;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

/**
 * 
 * @author S. Ricci
 *
 */
public class ExpressionEvaluator {
	
	private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d*(\\.\\d+)?$");
	private ExpressionFactory expressionFactory;

	public ExpressionEvaluator(ExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
	}

	public boolean evaluateBoolean(Node<?> context, Node<?> thisNode, String expression) throws InvalidExpressionException {
		return evaluateBoolean(context, thisNode, expression, false);
	}

	public boolean evaluateBoolean(Node<?> context, Node<?> thisNode, String expression, boolean normalizeNumbers) throws InvalidExpressionException {
		BooleanExpression expr = expressionFactory.createBooleanExpression(expression, normalizeNumbers);
		return expr.evaluate(context, thisNode);
	}

	public Object evaluateValue(Node<?> context, Node<?> thisNode, String expression) throws InvalidExpressionException {
		ValueExpression expr = expressionFactory.createValueExpression(expression);
		return expr.evaluate(context, thisNode);
	}
	
	public Number evaluateNumericValue(Node<?> context, Node<?> thisNode, String expression) throws InvalidExpressionException {
		if (isNumeric(expression)) {
			return Double.parseDouble(expression);
		} else {
			Object val = evaluateValue(context, thisNode, expression);
			return (Number) val;
		}
	}

	public Node<?> evaluateNode(Node<?> context, Node<?> thisNode, String expression) throws InvalidExpressionException {
		ModelPathExpression expr = expressionFactory.createModelPathExpression(expression);
		Node<?> node = expr.evaluate(context, thisNode);
		return node;
	}

	public List<Node<?>> evaluateNodes(Node<?> context, Node<?> thisNode, String expression) throws InvalidExpressionException {
		ModelPathExpression expr = expressionFactory.createModelPathExpression(expression);
		List<Node<?>> nodes = expr.iterate(context, thisNode);
		return nodes;
	}

	public List<Node<?>> evaluateAbsolutePath(Record record, String expression) throws InvalidExpressionException {
		AbsoluteModelPathExpression expr = expressionFactory.createAbsoluteModelPathExpression(expression);
		List<Node<?>> nodes = expr.iterate(record);
		return nodes;
	}
	
	public Set<String> determineReferencedPaths(String expression) throws InvalidExpressionException {
		ModelPathExpression expr = expressionFactory.createModelPathExpression(expression);
		return expr.getReferencedPaths();
	}

	public Set<NodeDefinition> determineReferencedNodeDefinitions(NodeDefinition context, String expr) throws InvalidExpressionException {
		ModelPathExpression modelPathExpression = expressionFactory.createModelPathExpression(expr);
		Set<NodeDefinition> referencedDefs = modelPathExpression.getReferencedNodeDefinitions(context);
		return referencedDefs;
	}

	private static boolean isNumeric(String expression) {
		Matcher matcher = NUMBER_PATTERN.matcher(expression);
		return matcher.matches();
	}
	
}
