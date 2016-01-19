/**
 *
 */
package org.openforis.idm.model.expression.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.jxpath.ri.JXPathCompiledExpression;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResult;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResultFlag;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * @author M. Togna
 * @author D. Wiell
 */
public class ModelJXPathCompiledExpression extends JXPathCompiledExpression {
	
	private final ReferencedPathEvaluator referencedPathEvaluator;
	private ExpressionFactory expressionFactory;

	public ModelJXPathCompiledExpression(ExpressionFactory expressionFactory, ReferencedPathEvaluator referencedPathEvaluator, String xpath, Expression expression) {
		super(xpath, expression);
		this.expressionFactory = expressionFactory;
		this.referencedPathEvaluator = referencedPathEvaluator;
	}

	public Set<String> getReferencedPaths() {
		return referencedPathEvaluator.determineReferencedPaths(getExpression());
	}

	public ExpressionValidationResult validate(final NodeDefinition contextNodeDef) {
		return validateOperations(new OperationVaildator() {
			public ExpressionValidationResult validate(Operation operation) {
				if (operation instanceof ModelExtensionFunction) {
					CustomFunction customFunction = expressionFactory.lookupFunction((ModelExtensionFunction) operation);
					if (null == customFunction) {
						String fullName = ((ModelExtensionFunction) operation).getFullName();
						String message = String.format("function '%s' does not exist", fullName);
						String functionNames = expressionFactory.getFullFunctionNames().toString();
						String detailedMessage = String.format("function '%s' does not exist\n Possible function names:\n%s", fullName, functionNames);
						return new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR, message, detailedMessage);
					} else {
						return customFunction.validateArguments(contextNodeDef, nullToEmpty(operation));
					}
				}
				return new ExpressionValidationResult();
			}

			private Expression[] nullToEmpty(Operation operation) {
				Expression[] arguments = operation.getArguments();
				if (arguments == null) {
					arguments = new Expression[0];
				}
				return arguments;
			}
		});
	}
	
	private ExpressionValidationResult validateOperations(OperationVaildator operationValidator) {
		Stack<Expression> stack = new Stack<Expression>();
		stack.push(getExpression());
		while (!stack.isEmpty()) {
			Expression expression = stack.pop();
			if (expression instanceof Operation) {
				ExpressionValidationResult result = operationValidator.validate((Operation) expression);
				if (result.isError()) {
					return result;
				}
			}
		}
		return new ExpressionValidationResult();
	}
	
	public Set<String> getFunctionNames() {
		Set<String> names = new HashSet<String>();
		Stack<Expression> stack = new Stack<Expression>();
		stack.push(getExpression());
		while (!stack.isEmpty()) {
			Expression expression = stack.pop();
			if (expression instanceof Operation) {
				if (expression instanceof CoreFunction || expression instanceof ModelExtensionFunction) {
					String name = expression.toString().replaceAll("\\(.*\\)", "");
					names.add(name);
				}
				Expression[] arguments = ((Operation) expression).getArguments();
				if (arguments != null && arguments.length > 0) {
					for (Expression arg : arguments) {
						stack.push(arg);
					}
				}
			}
		}
		return names;
	}
	
	private interface OperationVaildator {
		
		ExpressionValidationResult validate(Operation operation);
		
	}
}
