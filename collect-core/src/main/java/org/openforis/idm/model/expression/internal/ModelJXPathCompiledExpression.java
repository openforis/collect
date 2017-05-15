/**
 *
 */
package org.openforis.idm.model.expression.internal;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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
	
	private ExpressionFactory expressionFactory;

	public ModelJXPathCompiledExpression(ExpressionFactory expressionFactory, String xpath, Expression expression) {
		super(xpath, expression);
		this.expressionFactory = expressionFactory;
	}

	public Set<String> getReferencedPaths() {
		return expressionFactory.getReferencedPathEvaluator().determineReferencedPaths(getExpression());
	}

	public ExpressionValidationResult validate(final NodeDefinition contextNodeDef) {
		return validateOperations(new OperationVaildator() {
			public ExpressionValidationResult validate(Operation operation) {
				if (operation instanceof ModelExtensionFunction) {
					ModelExtensionFunction modelExtensionFun = (ModelExtensionFunction) operation;
					boolean valid = expressionFactory.isValidFunction(modelExtensionFun.getPrefix(), modelExtensionFun.getName());
					if (valid) {
						CustomFunction customFunction = expressionFactory.lookupFunction(modelExtensionFun);
						if (customFunction == null) {
							Expression[] arguments = modelExtensionFun.getArguments();
							int argumentsSize = arguments == null ? 0 : arguments.length;
							return new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR, 
									String.format("cannot invoke function %s passing %d arguments", 
											modelExtensionFun.getFullName(), argumentsSize));
						} else {
							return customFunction.validateArguments(contextNodeDef, nullToEmpty(operation));
						}
					} else {
						String fullName = modelExtensionFun.getPrefix() == null ? modelExtensionFun.getName() : modelExtensionFun.getFullName();
						String functionNames = expressionFactory.getFullFunctionNames().toString();
						String detailedMessage = String.format("function '%s' does not exist\n Possible function names:\n%s", fullName, functionNames);
						return new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR, detailedMessage);
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
		Deque<Expression> stack = new LinkedList<Expression>();
		stack.push(getExpression());
		while (!stack.isEmpty()) {
			Expression expression = stack.pop();
			if (expression instanceof Operation) {
				Operation op = (Operation) expression;
				ExpressionValidationResult result = operationValidator.validate(op);
				if (result.isError()) {
					return result;
				} else {
					Expression[] args = op.getArguments();
					if (args != null) {
						stack.addAll(Arrays.asList(args));
					}
				}
			}
		}
		return new ExpressionValidationResult();
	}
	
	public Set<String> getFunctionNames() {
		Set<String> names = new HashSet<String>();
		Deque<Expression> stack = new LinkedList<Expression>();
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
