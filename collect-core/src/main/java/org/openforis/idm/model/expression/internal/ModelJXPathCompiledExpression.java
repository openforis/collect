/**
 *
 */
package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.ri.JXPathCompiledExpression;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.Operation;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * @author M. Togna
 * @author D. Wiell
 */
public class ModelJXPathCompiledExpression extends JXPathCompiledExpression {
	private final ReferencedPathEvaluator referencedPathEvaluator;

	public ModelJXPathCompiledExpression(ReferencedPathEvaluator referencedPathEvaluator, String xpath, Expression expression) {
		super(xpath, expression);
		this.referencedPathEvaluator = referencedPathEvaluator;
	}

	public Set<String> getReferencedPaths() {
		return referencedPathEvaluator.determinePathsReferenced(getExpression());
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

}
