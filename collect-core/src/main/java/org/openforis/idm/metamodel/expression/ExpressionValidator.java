package org.openforis.idm.metamodel.expression;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.expression.BooleanExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.openforis.idm.model.expression.ValueExpression;

/**
 * Verifies that the expressions defined in a survey are valid
 *
 * @author S. Ricci
 */
public class ExpressionValidator {

	private ExpressionFactory expressionFactory;

	public ExpressionValidator(ExpressionFactory expressionFactory) {
		super();
		this.expressionFactory = expressionFactory;
	}

	public boolean validateBooleanExpression(NodeDefinition contextNodeDef, String expression) {
		try {
			BooleanExpression expr = expressionFactory.createBooleanExpression(expression);
			NodeDefinition parentDef = contextNodeDef.getParentDefinition();
			boolean result = expr.isSyntaxValid(parentDef);
			return result;
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
			return valueExpression.isSyntaxValid(parentNodeDef);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validateSchemaPathExpression(NodeDefinition contextNodeDef, String expression) {
		EntityDefinition parentDefinition = (EntityDefinition) contextNodeDef.getParentDefinition();
		try {
			ModelPathExpression pathExpression = expressionFactory.createModelPathExpression(expression);
			Set<String> referencedPaths = pathExpression.getReferencedPaths();
			for (String path : referencedPaths) {
				String normalizedPath = getNormalizedPath(path);
				SchemaPathExpression schemaExpression = new SchemaPathExpression(normalizedPath);
				schemaExpression.evaluate(parentDefinition);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean validateUniquenessExpression(NodeDefinition contextNodeDef, String expression) {
		try {
			ModelPathExpression pathExpression = expressionFactory.createModelPathExpression(expression);
			NodeDefinition parentDef = contextNodeDef.getParentDefinition();
			boolean result = pathExpression.isSyntaxValid(parentDef);
			return result;
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
		return path.replaceAll("\\$this/", "");
	}

}
