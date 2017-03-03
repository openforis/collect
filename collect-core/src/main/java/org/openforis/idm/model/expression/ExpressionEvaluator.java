package org.openforis.idm.model.expression;

import static org.openforis.idm.model.expression.Expressions.FALSE_FUNCTION;
import static org.openforis.idm.model.expression.Expressions.TRUE_FUNCTION;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.commons.collection.Predicate;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Value;

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
		if (TRUE_FUNCTION.equals(expression)) {
			return true;
		} else if (FALSE_FUNCTION.equals(expression)) {
			return false;
		}
		BooleanExpression expr = expressionFactory.createBooleanExpression(expression, normalizeNumbers);
		return expr.evaluate(context, thisNode);
	}

	public Object evaluateValue(Node<?> context, Node<?> thisNode, String expression) throws InvalidExpressionException {
		ValueExpression expr = expressionFactory.createValueExpression(expression);
		return expr.evaluate(context, thisNode);
	}
	
	@SuppressWarnings("unchecked")
	public <V extends Value> V evaluateAttributeValue(Node<?> context, Node<?> thisNode, AttributeDefinition defn, String expression) throws InvalidExpressionException {
		Object object = evaluateValue(context, thisNode, expression);
		if ( object == null ) {
			return null;
		} else {
			if ( object instanceof Value ) {
				Class<? extends Value> expectedValueType = defn.getValueType();
				if ( expectedValueType.isAssignableFrom(object.getClass()) ) {
					return (V) object;
				} else {
					throw new IllegalArgumentException(String.format("Unexpected value type. Found %s expected %s", expectedValueType.getName(), object.getClass().getName()));
				}
			} else {
				V val = defn.createValue(object);
				return val;
			}
		}
	}
	
	public Object evaluateFieldValue(Node<?> context, Node<?> thisNode, FieldDefinition<?> defn, String expression) throws InvalidExpressionException {
		Object value = evaluateValue(context, thisNode, expression);
		if ( value == null ) {
			return null;
		} else {
			Field<?> field = (Field<?>) defn.createNode();
			Object val = field.parseValue(value.toString());
			return val;
		}
	}
	
	public Number evaluateNumericValue(Node<?> context, Node<?> thisNode, String expression) throws InvalidExpressionException {
		if (isNumeric(expression)) {
			return Double.parseDouble(expression);
		} else {
			Object val = evaluateValue(context, thisNode, expression);
			return val == null ? null: val instanceof Number ?(Number) val: Double.parseDouble(val.toString());
		}
	}

	public Node<?> evaluateNode(Node<?> context, Node<?> thisNode, String expression) throws InvalidExpressionException {
		ModelPathExpression expr = expressionFactory.createModelPathExpression(expression);
		Node<?> node = expr.evaluate(context, thisNode);
		return node;
	}

	public List<Node<?>> evaluateNodes(Node<?> context, Node<?> thisNode, String expression) throws InvalidExpressionException {
		ModelPathExpression expr = expressionFactory.createModelPathExpression(expression);
		return expr.evaluateMultiple(context, thisNode);
	}

	public void iterateNodes(Node<?> context, Node<?> thisNode, String expression, NodeVisitor evaluator) throws InvalidExpressionException {
		ModelPathExpression expr = expressionFactory.createModelPathExpression(expression);
		expr.iterateMultiple(context, thisNode, evaluator);
	}
	
	public Node<?> findNode(Node<?> context, Node<?> thisNode, String expression, Predicate<Node<?>> evaluator) throws InvalidExpressionException {
		ModelPathExpression expr = expressionFactory.createModelPathExpression(expression);
		return expr.findNode(context, thisNode, evaluator);
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

	public Set<NodeDefinition> determineReferencedNodeDefinitions(NodeDefinition context, NodeDefinition thisNodeDef, String expr) throws InvalidExpressionException {
		ModelPathExpression modelPathExpression = expressionFactory.createModelPathExpression(expr);
		Set<NodeDefinition> referencedDefs = modelPathExpression.getReferencedNodeDefinitions(context, thisNodeDef);
		return referencedDefs;
	}
	
	private static boolean isNumeric(String expression) {
		Matcher matcher = NUMBER_PATTERN.matcher(expression);
		return matcher.matches();
	}
	
}
