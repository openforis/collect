/**
 *
 */
package org.openforis.idm.model.expression;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.internal.CustomFunction;
import org.openforis.idm.model.expression.internal.CustomFunctions;
import org.openforis.idm.model.expression.internal.EnvironmentFunctions;
import org.openforis.idm.model.expression.internal.GeoFunctions;
import org.openforis.idm.model.expression.internal.IDMFunctions;
import org.openforis.idm.model.expression.internal.MathFunctions;
import org.openforis.idm.model.expression.internal.ModelExtensionFunction;
import org.openforis.idm.model.expression.internal.ModelJXPathCompiledExpression;
import org.openforis.idm.model.expression.internal.ModelJXPathContext;
import org.openforis.idm.model.expression.internal.ModelNodePointerFactory;
import org.openforis.idm.model.expression.internal.NodePropertyHandler;
import org.openforis.idm.model.expression.internal.RecordPropertyHandler;
import org.openforis.idm.model.expression.internal.ReferencedPathEvaluator;
import org.openforis.idm.model.expression.internal.RegExFunctions;
import org.openforis.idm.model.expression.internal.UtilFunctions;
import org.openforis.idm.path.Path;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class ExpressionFactory {
	
	public static final String ENVIRONMENT_PREFIX = "env";
	public static final String GEO_PREFIX = "geo";
	public static final String IDM_PREFIX = "idm";
	public static final String MATH_PREFIX = "math";
	public static final String REGEX_PREFIX = "regex";
	public static final String UTIL_PREFIX = "util";

	private static final Set<String> CORE_FUNCTION_NAMES = new HashSet<String>(asList("boolean", "not", "true", "false", // boolean
																															// values
																															// functions
			"number", "round", "floor", "ceiling", "format-number", // math
																	// functions
			"string", "concat", "substring", "string-length", "normalize-space", "contains", "starts-with", "ends-with", // string
																															// functions
			"count", "sum", // aggregate functions
			"position", "last" // context functions
	));

	private static final ExpressionCache COMPILED_EXPRESSIONS = new ExpressionCache();

	private final Map<String, CustomFunctions> customFunctionsByNamespace = new HashMap<String, CustomFunctions>();
	private final ReferencedPathEvaluator referencedPathEvaluator;
	private ModelJXPathContext jxPathContext;
	private LookupProvider lookupProvider;

	public ExpressionFactory() {
		System.setProperty(JXPathContextFactory.FACTORY_NAME_PROPERTY,
				"org.openforis.idm.model.expression.internal.ModelJXPathContextFactory");

		JXPathContextReferenceImpl.addNodePointerFactory(new ModelNodePointerFactory());

		JXPathIntrospector.registerDynamicClass(Node.class, NodePropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(Record.class, RecordPropertyHandler.class);

		registerFunctions(new EnvironmentFunctions(ENVIRONMENT_PREFIX), new GeoFunctions(GEO_PREFIX), new IDMFunctions(IDM_PREFIX), new MathFunctions(MATH_PREFIX),
				new RegExFunctions(REGEX_PREFIX), new UtilFunctions(UTIL_PREFIX));

		referencedPathEvaluator = new ReferencedPathEvaluator(customFunctionsByNamespace);
	}

	public BooleanExpression createBooleanExpression(String expression) throws InvalidExpressionException {
		return createBooleanExpression(expression, false);
	}

	public BooleanExpression createBooleanExpression(String expression, boolean normalizeNumbers)
			throws InvalidExpressionException {
		ModelJXPathCompiledExpression compiledExpression = compileExpression(expression, normalizeNumbers);
		BooleanExpression expr = new BooleanExpression(compiledExpression, jxPathContext);
		return expr;
	}

	public ValueExpression createValueExpression(String expression) throws InvalidExpressionException {
		ModelJXPathCompiledExpression compiledExpression = compileExpression(expression);
		ValueExpression expr = new ValueExpression(compiledExpression, jxPathContext);
		return expr;
	}

	public ModelPathExpression createModelPathExpression(String expression) throws InvalidExpressionException {
		ModelJXPathCompiledExpression compiledExpression = compileExpression(expression);
		ModelPathExpression expr = new ModelPathExpression(compiledExpression, jxPathContext);
		return expr;
	}

	public AbsoluteModelPathExpression createAbsoluteModelPathExpression(String expression)
			throws InvalidExpressionException {
		if (!expression.startsWith(String.valueOf(Path.SEPARATOR))) {
			throw new InvalidExpressionException("Absolute paths must start with '/'");
		}
		int pos = expression.indexOf(Path.SEPARATOR, 1);
		if (pos < 0) {
			String root = expression.substring(1);
			return new AbsoluteModelPathExpression(root);
		} else {
			String root = expression.substring(1, pos);
			expression = expression.substring(pos + 1);
			ModelJXPathCompiledExpression compiledExpression = compileExpression(expression);
			return new AbsoluteModelPathExpression(root, compiledExpression, jxPathContext);
		}
	}

	public boolean isValidFunction(String namespace, String functionName) {
		boolean inDefaultNamespace = StringUtils.isBlank(namespace);
		if (inDefaultNamespace) {
			return CORE_FUNCTION_NAMES.contains(functionName);
		}
		CustomFunctions functions = customFunctionsByNamespace.get(namespace);
		return functions != null && functions.containsFunction(functionName);
	}

	public List<String> getFullFunctionNames() {
		List<String> result = new ArrayList<String>();
		result.addAll(CORE_FUNCTION_NAMES);
		Collections.sort(result);
		List<String> customFunctionNames = new ArrayList<String>();
		Set<Entry<String, CustomFunctions>> customFunctionEntries = customFunctionsByNamespace.entrySet();
		for (Entry<String, CustomFunctions> customFunctionEntry : customFunctionEntries) {
			Set<String> functionNames = customFunctionEntry.getValue().getFunctionNames();
			for (String functionName : functionNames) {
				customFunctionNames.add(customFunctionEntry.getKey() + ":" + functionName);
			}
		}
		Collections.sort(customFunctionNames);
		result.addAll(customFunctionNames);
		return result;
	}

	public void setLookupProvider(LookupProvider lookupProvider) {
		this.lookupProvider = lookupProvider;
		jxPathContext.setLookupProvider(lookupProvider);
	}

	public CustomFunction lookupFunction(ModelExtensionFunction modelExtensionFunction) {
		String namespace = modelExtensionFunction.getPrefix();
		CustomFunctions customFunctions = customFunctionsByNamespace.get(namespace);
		return (CustomFunction) customFunctions.getFunction(namespace, modelExtensionFunction.getName(),
				modelExtensionFunction.getArguments());
	}

	private void registerFunctions(CustomFunctions... functionsLibrary) {
		jxPathContext = (ModelJXPathContext) JXPathContext.newContext(null);
		FunctionLibrary library = new FunctionLibrary();
		for (CustomFunctions functions : functionsLibrary) {
			library.addFunctions(functions);
			String namespace = functions.getNamespace();
			if (customFunctionsByNamespace.containsKey(namespace)) {
				throw new IllegalStateException(
						String.format("Functions for namespace %s already registered", namespace));
			}
			customFunctionsByNamespace.put(namespace, functions);
		}
		jxPathContext.setFunctions(library);
	}

	private ModelJXPathCompiledExpression compileExpression(String expression) throws InvalidExpressionException {
		return compileExpression(expression, false);
	}

	private ModelJXPathCompiledExpression compileExpression(String expression, boolean normalizeNumber)
			throws InvalidExpressionException {
		ExpressionKey key = new ExpressionKey(expression, normalizeNumber);
		ModelJXPathCompiledExpression compiled = COMPILED_EXPRESSIONS.get(key);
		if (compiled == null) {
			try {
				String normalizedExpression = Path.getNormalizedPath(expression);
				compiled = ModelJXPathContext.compile(this, normalizedExpression, normalizeNumber);
				COMPILED_EXPRESSIONS.put(key, compiled);
			} catch (JXPathInvalidSyntaxException e) {
				throw new InvalidExpressionException(e.getMessage());
			}

		}
		return compiled;
	}

	public LookupProvider getLookupProvider() {
		return lookupProvider;
	}

	public ReferencedPathEvaluator getReferencedPathEvaluator() {
		return referencedPathEvaluator;
	}
	
	private static class ExpressionKey {

		private String expression;
		private boolean normalizeNumbers;

		public ExpressionKey(String expression, boolean normalizeNumbers) {
			super();
			this.expression = expression;
			this.normalizeNumbers = normalizeNumbers;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((expression == null) ? 0 : expression.hashCode());
			result = prime * result + (normalizeNumbers ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExpressionKey other = (ExpressionKey) obj;
			if (expression == null) {
				if (other.expression != null)
					return false;
			} else if (!expression.equals(other.expression))
				return false;
			if (normalizeNumbers != other.normalizeNumbers)
				return false;
			return true;
		}

	}

	private static class ExpressionCache extends LinkedHashMap<ExpressionKey, ModelJXPathCompiledExpression> {

		private static final long serialVersionUID = 1L;

		private static final int MAX_ENTRIES = 1000;

		@Override
		protected boolean removeEldestEntry(Entry<ExpressionKey, ModelJXPathCompiledExpression> eldest) {
			return size() > MAX_ENTRIES;
		}

	}

}
