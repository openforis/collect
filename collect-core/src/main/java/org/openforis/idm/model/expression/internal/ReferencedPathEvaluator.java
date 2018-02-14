package org.openforis.idm.model.expression.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.openforis.idm.path.Path;

/**
 * @author M. Togna
 * @author D. Wiell
 * @author S. Ricci
 */
public class ReferencedPathEvaluator {
	
	private final Map<String, CustomFunctions> customFunctionsByNamespace;

	public ReferencedPathEvaluator(Map<String, CustomFunctions> customFunctionsByNamespace) {
		this.customFunctionsByNamespace = customFunctionsByNamespace;
	}

	public Set<String> determineReferencedPaths(Expression expression) {
		if (expression instanceof ExpressionPath) {
			return determineReferencedPaths((ExpressionPath) expression);
		} else if (expression instanceof org.apache.commons.jxpath.ri.compiler.Path) {
			return determineReferencedPaths((org.apache.commons.jxpath.ri.compiler.Path) expression);
		} else if (expression instanceof Operation) {
			return determineReferencedPaths((Operation) expression);
		} else {
			return Collections.emptySet(); // TODO: Can this happen?
		}
	}

	private Set<String> determineReferencedPaths(ExpressionPath expressionPath) {
		Set<String> paths = new HashSet<String>(determineReferencedPaths((org.apache.commons.jxpath.ri.compiler.Path) expressionPath));
		for (Expression p : expressionPath.getPredicates()) {
			paths.addAll(determineReferencedPaths(p));
		}
		return paths;
	}
	
	private Set<String> determineReferencedPaths(org.apache.commons.jxpath.ri.compiler.Path modelLocationPath) {
		Set<String> paths = new HashSet<String>();
		if (modelLocationPath.getSteps().length > 0) {
			paths.add(Path.getAbsolutePath(modelLocationPath.toString()));
			StringBuilder predicateBasePathSB = new StringBuilder();
			for (Step step : modelLocationPath.getSteps()) {
				String stepVal = step.toString();
				predicateBasePathSB.append(Path.getAbsolutePath(stepVal)).append(Path.SEPARATOR);
				for (Expression predicate : step.getPredicates()) {
					Set<String> predicatePaths = determineReferencedPaths(predicate);
					for (String predicateReferencePath : predicatePaths) {
						if (predicateReferencePath.startsWith(Path.THIS_VARIABLE)
								|| predicateReferencePath.startsWith(Path.CONTEXT_VARIABLE)) {
							paths.add(predicateReferencePath);
						} else {
							paths.add(predicateBasePathSB.toString() + predicateReferencePath);
						}
					}
				}
			}
		}
		return paths;
	}
	
	private Set<String> determineReferencedPaths(Operation operation) {
		Set<String> paths = new HashSet<String>();
		if (operation instanceof ModelExtensionFunction) {
			ModelExtensionFunction modelExtensionFunction = (ModelExtensionFunction) operation;
			Set<String> pathsReferencedByFunction = determineReferencedPaths(modelExtensionFunction);
			paths.addAll(pathsReferencedByFunction);
		}
		Expression[] arguments = operation.getArguments();
		boolean hasArguments = arguments != null && arguments.length > 0;
		if (hasArguments) {
			for (Expression arg : arguments) {
				paths.addAll(determineReferencedPaths(arg));
			}
		}
		return paths;
	}

	private Set<String> determineReferencedPaths(ModelExtensionFunction function) {
		String namespace = function.getFunctionName().getPrefix();
		CustomFunctions functions = customFunctionsByNamespace.get(namespace);
		if (functions == null) {
			return Collections.emptySet();
		}
		int parameterCount = function.getArguments() == null ? 0 : function.getArguments().length;
		String functionName = function.getFunctionName().getName();
		return functions.getPathsReferencedByFunction(functionName, parameterCount);
	}
}
