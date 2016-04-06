package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Functions;

import java.util.*;

public abstract class CustomFunctions implements Functions {
	private final Map<FunctionKey, CustomFunction> functions = new HashMap<FunctionKey, CustomFunction>();
	private final Set<String> functionNames = new HashSet<String>();
	private final String namespace;

	public CustomFunctions(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public final Set<String> getUsedNamespaces() {
		return Collections.singleton(namespace);
	}

	public String getNamespace() {
		return namespace;
	}

	public Set<String> getFunctionNames() {
		return functionNames;
	}
	
	public final boolean containsFunction(String functionName) {
		return functionNames.contains(functionName);
	}

	@Override
	public final Function getFunction(String namespace, String name, Object[] parameters) {
		int parameterCount = parameters == null ? 0 : parameters.length;
		return functions.get(new FunctionKey(name, parameterCount));
	}

	public Set<String> getPathsReferencedByFunction(String functionName, int parameterCount) {
		CustomFunction function = functions.get(new FunctionKey(functionName, parameterCount));
		if (function == null) {
			return Collections.emptySet();
		}
		return function.getReferencedPaths();
	}

	private final void register(String name, int parameterCount, CustomFunction function) {
		functions.put(new FunctionKey(name, parameterCount), function);
		functionNames.add(name);
	}
	
	protected final void register(String name, CustomFunction function) {
		for (Integer parameterCount : function.getSupportedArgumentCounts()) {
			register(name, parameterCount, function);
		}
	}

	private static class FunctionKey {
		public final String name;
		public final int parameterCount;

		public FunctionKey(String name, int parameterCount) {
			this.name = name;
			this.parameterCount = parameterCount;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			FunctionKey that = (FunctionKey) o;

			if (parameterCount != that.parameterCount) {
				return false;
			}
			if (name != null ? !name.equals(that.name) : that.name != null) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = name != null ? name.hashCode() : 0;
			result = 31 * result + parameterCount;
			return result;
		}

		@Override
		public String toString() {
			return "FunctionKey{" +
					"name='" + name + '\'' +
					", parameterCount=" + parameterCount +
					'}';
		}
	}
}
