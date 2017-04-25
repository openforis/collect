/**
 * 
 */
package org.openforis.idm.metamodel.expression;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.Variables;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.expression.internal.NodeDefinitionPropertyHandler;
import org.openforis.idm.metamodel.expression.internal.SchemaPropertyHandler;
import org.openforis.idm.model.expression.AbstractExpression;
import org.openforis.idm.path.Path;

/**
 * @author M. Togna
 * 
 */
abstract class AbstractSchemaExpression {

	private static JXPathContext CONTEXT;

	static {
		JXPathIntrospector.registerDynamicClass(NodeDefinition.class, NodeDefinitionPropertyHandler.class);
		JXPathIntrospector.registerDynamicClass(Schema.class, SchemaPropertyHandler.class);

		CONTEXT = JXPathContext.newContext(null);
	}

	private String expression;

	AbstractSchemaExpression(String expression) {
		this.expression = expression;
	}

	public Object evaluate(NodeDefinition context, NodeDefinition thisNode) {
		if (!(Schema.class.isAssignableFrom(context.getClass()) || NodeDefinition.class.isAssignableFrom(context.getClass()))) {
			throw new IllegalArgumentException("Unable to evaluate expression with context class " + context.getClass().getName());
		}
		JXPathContext jxPathContext = JXPathContext.newContext(CONTEXT, context);
		Variables variables = jxPathContext.getVariables();
		variables.declareVariable(AbstractExpression.CONTEXT_NODE_VARIABLE_NAME, context);
		variables.declareVariable(AbstractExpression.THIS_VARIABLE_NAME, thisNode);
		
		String expr = Path.getNormalizedPath(expression);
		Object result = jxPathContext.getValue(expr);
		return result;
	}

}
