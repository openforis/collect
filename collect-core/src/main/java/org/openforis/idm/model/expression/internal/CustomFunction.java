package org.openforis.idm.model.expression.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResult;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResultFlag;

public abstract class CustomFunction implements Function {
	
	private static final int UNLIMITED_ARGUMENTS_COUNT = Integer.MAX_VALUE;
	private static final List<Integer> UNLIMITED_ARGUMENT_COUNTS = Arrays.asList(UNLIMITED_ARGUMENTS_COUNT);
	
	private final Set<String> referencedPaths;
	private final List<Integer> supportedArgumentCounts;

	/**
	 * Create a custom function, optionally including paths that are referenced
	 * independent on any parameters passed to the function.
	 */
	public CustomFunction(String... referencedPaths) {
		this(UNLIMITED_ARGUMENT_COUNTS, referencedPaths);
	}
	
	public CustomFunction(int supportedArgumentCount, String... referencedPaths) {
		this(Arrays.asList(supportedArgumentCount), referencedPaths);
	}

	public CustomFunction(List<Integer> supportedArgumentCounts, String... referencedPaths) {
		this.supportedArgumentCounts = supportedArgumentCounts;
		Set<String> paths = new HashSet<String>();
		Collections.addAll(paths, referencedPaths);
		this.referencedPaths = Collections.unmodifiableSet(paths);
	}

	public List<Integer> getSupportedArgumentCounts() {
		return supportedArgumentCounts;
	}

	public final ExpressionValidationResult validateArguments(NodeDefinition contextNodeDef, Expression[] arguments) {
		if (getSupportedArgumentCounts().contains(arguments.length) 
				|| UNLIMITED_ARGUMENT_COUNTS.equals(getSupportedArgumentCounts())) {
			return performArgumentValidation(contextNodeDef, arguments);
		} else {
			return new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR,
					String.format("Invalid number of arguments: found %d %s expected", 
							arguments.length, getSupportedArgumentCounts().toString()));
		}
	}
	
	protected ExpressionValidationResult performArgumentValidation(NodeDefinition contextNodeDef, Expression[] arguments) {
		return new ExpressionValidationResult();
	}

	public Set<String> getReferencedPaths() {
		return referencedPaths;
	}
}
