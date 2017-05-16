package org.openforis.idm.metamodel.expression;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.expression.AbstractExpression;
import org.openforis.idm.model.expression.BooleanExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.openforis.idm.model.expression.ValueExpression;

/**
 * Verifies that the expressions defined in a survey are valid
 *
 * @author S. Ricci
 */
public class ExpressionValidator {

	private static final Pattern FUNCTION_NAME_PATTERN = Pattern.compile("((\\w+):)?([a-zA-Z0-9-_]+)");

	public enum ExpressionType {
		BOOLEAN, VALUE, SCHEMA_PATH
	}
	
	public enum ExpressionValidationResultFlag {
		OK, ERROR
	}
	
	private ExpressionFactory expressionFactory;

	public ExpressionValidator(ExpressionFactory expressionFactory) {
		super();
		this.expressionFactory = expressionFactory;
	}
	
	public ExpressionValidationResult validateExpression(ExpressionType type, NodeDefinition contextNodeDef, 
			NodeDefinition thisNodeDef, String expression) {
		switch(type) {
		case BOOLEAN:
			return validateBooleanExpression(contextNodeDef, thisNodeDef, expression);
		case SCHEMA_PATH:
			return validateSchemaPathExpression(contextNodeDef, thisNodeDef, expression);
		case VALUE:
			return validateValueExpression(contextNodeDef, thisNodeDef, expression);
		default:
			throw new IllegalArgumentException("Expression type not supported: " + type.name());
		}
	}
	
	public ExpressionValidationResult validateBooleanExpression(NodeDefinition thisNodeDef, String expression) {
		return validateBooleanExpression(thisNodeDef.getParentDefinition(), thisNodeDef, expression);
	}
	
	public ExpressionValidationResult validateBooleanExpression(NodeDefinition contextNodeDef, NodeDefinition thisNodeDef, String expression) {
		try {
			BooleanExpression expr = expressionFactory.createBooleanExpression(expression);
			return validateSyntax(contextNodeDef, thisNodeDef, expr);
		} catch (Exception e) {
			return createErrorValidationResult(e);
		}
	}

	public ExpressionValidationResult validateValueExpression(NodeDefinition contextNodeDef, String expression) {
		return validateValueExpression(contextNodeDef.getParentDefinition(), contextNodeDef, expression);
	}

	public ExpressionValidationResult validateValueExpression(NodeDefinition parentNodeDef, NodeDefinition thisNodeDef, String expression) {
		try {
			ValueExpression valueExpression = expressionFactory.createValueExpression(expression);
			return validateSyntax(parentNodeDef, thisNodeDef, valueExpression);
		} catch (Exception e) {
			return createErrorValidationResult(e);
		}
	}

	public ExpressionValidationResult validateSchemaPathExpression(NodeDefinition contextNodeDef, String expression) {
		return validateSchemaPathExpression(contextNodeDef.getParentDefinition(), contextNodeDef, expression);
	}
	
	public ExpressionValidationResult validateSchemaPathExpression(NodeDefinition contextNodeDef, NodeDefinition thisNodeDef, String expression) {
		try {
			AbstractExpression pathExpression = expressionFactory.createModelPathExpression(expression);
			Set<String> referencedPaths = pathExpression.getReferencedPaths();
			for (String path : referencedPaths) {
				SchemaPathExpression schemaExpression = new SchemaPathExpression(path);
				try {
					schemaExpression.evaluate(contextNodeDef, thisNodeDef);
				} catch (Exception e) {
					return createErrorValidationResult(e);
				}
			}
			return new ExpressionValidationResult();
		} catch (Exception e) {
			return createErrorValidationResult(e);
		}
	}

	public ExpressionValidationResult validateUniquenessExpression(NodeDefinition parentNodeDef, NodeDefinition contextNodeDef, String expression) {
		return validteModelPathExpression(parentNodeDef, contextNodeDef, expression);
	}

	public ExpressionValidationResult validateRegularExpression(String regEx) {
		try {
			Pattern.compile(regEx);
			return new ExpressionValidationResult();
		} catch (Exception e) {
			return createErrorValidationResult(e);
		}
	}

	public ExpressionValidationResult validateCircularReferenceAbsence(NodeDefinition thisNodeDef, String expression) {
		return validateCircularReferenceAbsence(thisNodeDef.getParentDefinition(), thisNodeDef, expression);
	}
	
	private ExpressionValidationResult validteModelPathExpression(NodeDefinition parentNodeDef, NodeDefinition contextNodeDef, String expression) {
		try {
			ModelPathExpression pathExpression = expressionFactory.createModelPathExpression(expression);
			return validateSyntax(parentNodeDef, contextNodeDef, pathExpression);
		} catch (Exception e) {
			return createErrorValidationResult(e);
		}
	}
	
	private ExpressionValidationResult validateSyntax(NodeDefinition contextNodeDef, NodeDefinition thisNodeDef, AbstractExpression expression) {
		ExpressionValidationResult result = expression.validate(contextNodeDef);
		if (result.isError()) {
			return result;
		}
		result = validatePaths(contextNodeDef, thisNodeDef, expression);
		return result;
	}
	
	public boolean isFunctionNameValid(AbstractExpression expression, String name) {
		Matcher matcher = FUNCTION_NAME_PATTERN.matcher(name);
		boolean valid;
		if (matcher.matches()) {
			String namespace = matcher.group(2);
			String functionName = matcher.group(3);
			valid = expressionFactory.isValidFunction(namespace, functionName);
		} else {
			valid = false;
		}
		return valid;
	}

	private ExpressionValidationResult validatePaths(NodeDefinition context, NodeDefinition thisNodeDef, AbstractExpression expression) {
		try {
			expression.getReferencedNodeDefinitions(context, thisNodeDef);
			return new ExpressionValidationResult();
		} catch (InvalidExpressionException e) {
			return createErrorValidationResult(e);
		}
	}
	
	//TODO restore this validation
	public ExpressionValidationResult validateCircularReferenceAbsence(NodeDefinition context, NodeDefinition node, String expression) {
		return new ExpressionValidationResult();
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
	private ExpressionValidationResult createErrorValidationResult(Exception e) {
		ExpressionValidationResult result = new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR, e.getMessage());
		if (e instanceof InvalidExpressionException) {
			result.setDetailedMessage(((InvalidExpressionException) e).getDetailedMessage());
		}
		return result;
	}

	public static class ExpressionValidationResult {
		
		private ExpressionValidationResultFlag flag;
		private String message;
		private String[] messageArgs;
		private String detailedMessage;
		private String[] detailedMessageArgs;
		
		public ExpressionValidationResult() {
			this.flag = ExpressionValidationResultFlag.OK;
		}
		
		public ExpressionValidationResult(ExpressionValidationResultFlag flag,
				String message, String... messageArgs) {
			super();
			this.flag = flag;
			this.message = message;
			this.messageArgs = messageArgs;
		}
		
		public boolean isOk() {
			return flag == ExpressionValidationResultFlag.OK;
		}
		
		public boolean isError() {
			return flag == ExpressionValidationResultFlag.ERROR;
		}

		public ExpressionValidationResultFlag getFlag() {
			return flag;
		}
		
		public void setFlag(ExpressionValidationResultFlag flag) {
			this.flag = flag;
		}
		
		public String getMessage() {
			return message;
		}
		
		public void setMessage(String message) {
			this.message = message;
		}
		
		public String[] getMessageArgs() {
			return messageArgs;
		}
		
		public void setMessageArgs(String[] messageArgs) {
			this.messageArgs = messageArgs;
		}
		
		public String getDetailedMessage() {
			return detailedMessage;
		}
		
		public void setDetailedMessage(String detailedMessage) {
			this.detailedMessage = detailedMessage;
		}
		
		public String[] getDetailedMessageArgs() {
			return detailedMessageArgs;
		}
		
		public void setDetailedMessageArgs(String[] detailedMessageArgs) {
			this.detailedMessageArgs = detailedMessageArgs;
		}
		
	}

}
