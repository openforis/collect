/**
 *
 */
package org.openforis.idm.model.expression;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.VariablePointer;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.expression.internal.ModelJXPathCompiledExpression;
import org.openforis.idm.model.expression.internal.ModelJXPathContext;
import org.openforis.idm.model.expression.internal.ModelNodePointer;
import org.openforis.idm.path.Path;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author M. Togna
 * @author G. Miceli
 */
abstract class AbstractExpression {

	private static final String THIS_VARIABLE_NAME = "this";
	private static final Pattern FUNCTION_NAME_PATTERN = Pattern.compile("((\\w+):)?(\\w+)");
	private static final QName THIS = new QName(THIS_VARIABLE_NAME);
	private ModelJXPathContext jxPathContext;
	private ModelJXPathCompiledExpression compiledExpression;

	AbstractExpression(ModelJXPathCompiledExpression compiledExpression, ModelJXPathContext jxPathContext) {
		this.compiledExpression = compiledExpression;
		this.jxPathContext = jxPathContext;
	}

	/**
	 * Returns the list of reference paths of this expression
	 */
	public Set<String> getReferencedPaths() {
		Set<String> paths = compiledExpression.getReferencedPaths();
		return CollectionUtils.unmodifiableSet(paths);
	}


	public Set<NodeDefinition> getReferencedNodeDefinitions(NodeDefinition context) throws InvalidExpressionException {
		Set<NodeDefinition> result = new HashSet<NodeDefinition>();
		Set<String> paths = compiledExpression.getReferencedPaths();
		for (String path : paths) {
			NodeDefinition nodeDef = getReferencedNodeDefinition(context, path);
			result.add(nodeDef);
		}
		return result;
	}

	private NodeDefinition getReferencedNodeDefinition(NodeDefinition context, String path) throws InvalidExpressionException {
		StringTokenizer tokenizer = new StringTokenizer(path, "/");
		NodeDefinition definition = context;
		while (tokenizer.hasMoreTokens()) {
			String section = tokenizer.nextToken();
			definition = getChildDefinition(definition, section);
		}
		return definition;
	}

	protected Object evaluateSingle(Node<?> contextNode, Node<?> thisNode) throws InvalidExpressionException {
		try {
			JXPathContext jxPathContext = createJXPathContext(contextNode, thisNode);
			Object object = compiledExpression.getValue(jxPathContext);
			return object;
		} catch (IllegalArgumentException e) {
			throw new InvalidExpressionException("Invalid path " + this.compiledExpression.toString());
		} catch (JXPathInvalidSyntaxException e) {
			throw new InvalidExpressionException(e.getMessage());
		} catch (JXPathNotFoundException e) {
			return null;
		}
	}

	/**
	 * Returns a list of Node that matches the expression
	 *
	 * @throws InvalidExpressionException
	 */
	protected List<Node<?>> evaluateMultiple(Node<?> contextNode, Node<?> thisNode) throws InvalidExpressionException {
		try {
			List<Node<?>> list = new ArrayList<Node<?>>();
			JXPathContext jxPathContext = createJXPathContext(contextNode, thisNode);

			Iterator<?> pointers = compiledExpression.iteratePointers(jxPathContext);
			while (pointers.hasNext()) {
				Object pointer = pointers.next();
				if (pointer instanceof ModelNodePointer) {
					ModelNodePointer modelNodePointer = (ModelNodePointer) pointer;
					Object ptrNode = modelNodePointer.getNode();
					if (ptrNode != null && ptrNode instanceof Node) {
						Node<?> n = (Node<?>) ptrNode;
						list.add(n);
					}
				} else if (pointer instanceof VariablePointer && ((VariablePointer) pointer).getName().equals(THIS)) {
					list.add(thisNode);
				}
				// ignore node pointer if it's a NullPointer
			}
			return list;
		} catch (IllegalArgumentException e) {
			throw new InvalidExpressionException("Invalid path " + this.compiledExpression.toString());
		} catch (JXPathInvalidSyntaxException e) {
			throw new InvalidExpressionException(e.getMessage());
		}
	}

	/**
	 * Verifies that the reference paths of this expression matches the contextNodeDefinition
	 *
	 * @throws InvalidExpressionException if the path is invalid
	 */
	private void verifyPaths(NodeDefinition context) throws InvalidExpressionException {
		//try to get referenced node definitions
		getReferencedNodeDefinitions(context);
	}

	private NodeDefinition getChildDefinition(NodeDefinition parent, String pathSection) throws InvalidExpressionException {
		if (Path.NORMALIZED_PARENT_FUNCTION.equals(pathSection)) {
			return parent.getParentDefinition();
		} else if ("$this".equals(pathSection)) {
			return parent;
		} else {
			String childName = pathSection.replaceAll("\\[.+]", "");
			if (parent instanceof EntityDefinition) {
				try {
					NodeDefinition childDefinition = ((EntityDefinition) parent).getChildDefinition(childName);
					return childDefinition;
				} catch (Exception e) {
					throw new InvalidExpressionException("Invalid path " + compiledExpression.toString());
				}
			}
			throw new InvalidExpressionException("Invalid path " + compiledExpression.toString());
		}
	}

	/**
	 * Creates a new JXPath context in order to evaluate the expression
	 */
	private JXPathContext createJXPathContext(Node<?> contextNode, Node<?> thisNode) {
		ModelJXPathContext jxPathContext = ModelJXPathContext.newContext(this.jxPathContext, contextNode);
		if (thisNode != null) {
			Variables variables = jxPathContext.getVariables();
			variables.declareVariable(THIS_VARIABLE_NAME, thisNode);
		}
		return jxPathContext;
	}

	public boolean isSyntaxValid(NodeDefinition contextNodeDef) {
		try {
			ExpressionFactory expressionFactory = contextNodeDef.getSurvey().getContext().getExpressionFactory();
			verifyFunctionNames(expressionFactory);
			verifyPaths(contextNodeDef);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void verifyFunctionNames(ExpressionFactory expressionFactory) throws InvalidExpressionException {
		Set<String> names = compiledExpression.getFunctionNames();
		for (String name : names) {
			verifyFunctionName(name, expressionFactory);
		}
	}

	private void verifyFunctionName(String name, ExpressionFactory expressionFactory)
			throws InvalidExpressionException {
		Matcher matcher = FUNCTION_NAME_PATTERN.matcher(name);
		boolean valid;
		if (matcher.matches()) {
			String namespace = matcher.group(2);
			String functionName = matcher.group(3);
			valid = expressionFactory.isFunction(namespace, functionName);
		} else {
			valid = false;
		}
		if (!valid) {
			throw new InvalidExpressionException(String.format("Invalid function '%s' in %s", name, compiledExpression.toString()));
		}
	}

}
