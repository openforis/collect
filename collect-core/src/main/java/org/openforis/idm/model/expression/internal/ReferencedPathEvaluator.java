package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.openforis.idm.path.Path;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author M. Togna
 * @author D. Wiell
 */
public class ReferencedPathEvaluator {
	private final Map<String, CustomFunctions> customFunctionsByNamespace;

	public ReferencedPathEvaluator(Map<String, CustomFunctions> customFunctionsByNamespace) {
		this.customFunctionsByNamespace = customFunctionsByNamespace;
	}

	public Set<String> determinePathsReferenced(Expression expression) {
		if (expression instanceof ModelLocationPath) {
			return determinePathsReferencedByModelLocationPath((ModelLocationPath) expression);
		}
		if (expression instanceof Operation) {
			return determinePathsReferencedByOperation((Operation) expression);
		}
		return Collections.emptySet(); // TODO: Can this happen?
	}

	private Set<String> determinePathsReferencedByModelLocationPath(ModelLocationPath modelLocationPath) {
		Set<String> paths = new HashSet<String>();
		paths.add(Path.getAbsolutePath(modelLocationPath.toString()));
		String predicateBasePath = "";
		for (Step step : modelLocationPath.getSteps()) {
			predicateBasePath += Path.getAbsolutePath(step.toString()) + "/";
			for (Expression predicate : step.getPredicates()) {
				Set<String> predicatePaths = determinePathsReferenced(predicate);
				for (String predicateReferencePath : predicatePaths) {
					paths.add(predicateBasePath + predicateReferencePath);
				}
			}
		}
		return paths;
	}

	private Set<String> determinePathsReferencedByOperation(Operation operation) {
		Expression[] arguments = operation.getArguments();
		boolean hasArguments = arguments != null && arguments.length > 0;
		Set<String> paths = new HashSet<String>();
		if (operation instanceof ModelExtensionFunction) {
			ModelExtensionFunction modelExtensionFunction = (ModelExtensionFunction) operation;
			Set<String> pathsReferencedByFunction = determinePathsReferencedByCustomFunction(modelExtensionFunction);
			paths.addAll(pathsReferencedByFunction);
		}
		if (hasArguments) {
			for (Expression arg : arguments) {
				paths.addAll(determinePathsReferenced(arg));
			}
		}
		return paths;
	}

	private Set<String> determinePathsReferencedByCustomFunction(ModelExtensionFunction function) {
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
