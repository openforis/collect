/**
 * 
 */
package org.openforis.idm.metamodel.expression;

import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author M. Togna
 * 
 */
public class SchemaPathExpression extends AbstractSchemaExpression {

	public SchemaPathExpression(String expression) {
		super(expression);
	}

	public NodeDefinition evaluate(NodeDefinition context) {
		NodeDefinition nodeDefinition = (NodeDefinition) super.evaluate(context);
		return nodeDefinition;
	}

}
