/**
 *
 */
package org.openforis.idm.model.expression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

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

/**
 * @author M. Togna
 * @author G. Miceli
 */
public abstract class AbstractExpression {

	private static final String THIS_VARIABLE_NAME = "this";
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

	public Set<String> getFunctionNames() {
		return compiledExpression.getFunctionNames();
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

	private NodeDefinition getChildDefinition(NodeDefinition parent, String pathSection) throws InvalidExpressionException {
		if (Path.NORMALIZED_PARENT_FUNCTION.equals(pathSection)) {
			return parent.getParentDefinition();
		} else if (Path.THIS_ALIASES.contains(pathSection)) {
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

}
