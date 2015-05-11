package org.openforis.idm.metamodel.expression;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.expression.AbstractExpression;
import org.openforis.idm.model.expression.BooleanExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.openforis.idm.model.expression.ValueExpression;
import org.openforis.idm.path.Path;

/**
 * Verifies that the expressions defined in a survey are valid
 *
 * @author S. Ricci
 */
public class ExpressionValidator {

	private static final Pattern FUNCTION_NAME_PATTERN = Pattern.compile("((\\w+):)?([a-zA-Z0-9-_]+)");
	
	private ExpressionFactory expressionFactory;

	public ExpressionValidator(ExpressionFactory expressionFactory) {
		super();
		this.expressionFactory = expressionFactory;
	}

	public boolean validateBooleanExpression(NodeDefinition contextNodeDef, String expression) {
		try {
			BooleanExpression expr = expressionFactory.createBooleanExpression(expression);
			return isSyntaxValid(contextNodeDef.getParentDefinition(), contextNodeDef, expr);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validateValueExpression(NodeDefinition contextNodeDef, String expression) {
		NodeDefinition parentDef = contextNodeDef.getParentDefinition();
		return validateValueExpression(contextNodeDef, parentDef, expression);
	}

	public boolean validateValueExpression(NodeDefinition contextNodeDef, NodeDefinition parentNodeDef, String expression) {
		try {
			ValueExpression valueExpression = expressionFactory.createValueExpression(expression);
			return isSyntaxValid(parentNodeDef, contextNodeDef, valueExpression);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validateSchemaPathExpression(NodeDefinition contextNodeDef, String expression) {
		try {
			EntityDefinition parentDefinition = (EntityDefinition) contextNodeDef.getParentDefinition();
			AbstractExpression pathExpression = expressionFactory.createModelPathExpression(expression);
			Set<String> referencedPaths = pathExpression.getReferencedPaths();
			for (String path : referencedPaths) {
				String normalizedPath = getNormalizedPath(path);
				SchemaPathExpression schemaExpression = new SchemaPathExpression(normalizedPath);
				schemaExpression.evaluate(parentDefinition, contextNodeDef);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validateUniquenessExpression(NodeDefinition parentNodeDef, NodeDefinition contextNodeDef, String expression) {
		try {
			return isModelPathSyntaxValid(parentNodeDef, contextNodeDef, expression);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validateRegularExpression(String regEx) {
		try {
			Pattern.compile(regEx);
			return true;
		} catch (PatternSyntaxException e) {
			return false;
		}
	}

	public boolean validateCircularReferenceAbsence(NodeDefinition node, String expression) {
		return validateCircularReferenceAbsence(node.getParentDefinition(), node, expression);
	}
	
	private boolean isModelPathSyntaxValid(NodeDefinition parentNodeDef, NodeDefinition contextNodeDef, String expression) {
		try {
			ModelPathExpression pathExpression = expressionFactory.createModelPathExpression(expression);
			return isSyntaxValid(parentNodeDef, contextNodeDef, pathExpression);
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean isSyntaxValid(NodeDefinition contextNodeDef, NodeDefinition thisNodeDef, AbstractExpression expression) {
		try {
			verifyFunctionNames(expression);
			verifyPaths(contextNodeDef, thisNodeDef, expression);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void verifyFunctionNames(AbstractExpression expression) throws InvalidExpressionException {
		Set<String> names = expression.getFunctionNames();
		for (String name : names) {
			verifyFunctionName(expression, name);
		}
	}

	public void verifyFunctionName(AbstractExpression expression, String name)
			throws InvalidExpressionException {
		Matcher matcher = FUNCTION_NAME_PATTERN.matcher(name);
		boolean valid;
		if (matcher.matches()) {
			String namespace = matcher.group(2);
			String functionName = matcher.group(3);
			valid = expressionFactory.isValidFunction(namespace, functionName);
		} else {
			valid = false;
		}
		if (!valid) {
			throw new InvalidExpressionException(String.format("Invalid function '%s' in %s", name, expression));
		}
	}

	/**
	 * Verifies that the reference paths of this expression matches the contextNodeDefinition
	 *
	 * @throws InvalidExpressionException if the path is invalid
	 */
	private void verifyPaths(NodeDefinition context, NodeDefinition thisNodeDef, AbstractExpression expression) throws InvalidExpressionException {
		//try to get referenced node definitions
		expression.getReferencedNodeDefinitions(context, thisNodeDef);
	}
	
	//TODO restore this validation
	public boolean validateCircularReferenceAbsence(NodeDefinition context, NodeDefinition node, String expression) {
		return true;
//		try {
//			Set<NodeDefinition> referencedNodes = calculateReferencedNodes(context, expression);
//			if ( referencedNodes.isEmpty() ) {
//				return true;
//			} else if ( referencedNodes.contains(node) ) {
//				return false;
//			} else {
//				// check references in calculated attributes
//				Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
//				stack.addAll(referencedNodes);
//				while ( ! stack.isEmpty() ) {
//					NodeDefinition currentNode = stack.pop();
//					if ( currentNode == node ) {
//						return false;
//					} else {
//						if ( currentNode instanceof AttributeDefinition && ((AttributeDefinition) currentNode).isCalculated() ) {
//							List<AttributeDefault> attributeDefaults = ((AttributeDefinition) currentNode).getAttributeDefaults();
//							for (AttributeDefault attributeDefault : attributeDefaults) {
//								stack.addAll(attributeDefault.determineReferencedNodes(currentNode.getParentDefinition()));
//							}
//						}
//					}
//				}
//				return true;
//			}
//		} catch (InvalidExpressionException e) {
//			return false;
//		}
	}

	//	private Set<NodeDefinition> calculateReferencedNodes(NodeDefinition context, String expression) throws InvalidExpressionException {
//		if ( StringUtils.isBlank(expression) ) {
//			return Collections.emptySet();
//		}
//		ModelPathExpression modelPathExpression = expressionFactory.createModelPathExpression(expression);
//		Set<NodeDefinition> referencedNodes = modelPathExpression.getReferencedNodeDefinitions(context);
//		return referencedNodes;
//	}
//	
	private String getNormalizedPath(String path) {
		return Path.removeThisVariableToken(path);
	}

}
