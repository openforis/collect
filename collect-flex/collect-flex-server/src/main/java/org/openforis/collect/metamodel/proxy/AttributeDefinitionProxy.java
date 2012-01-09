/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.openforis.idm.metamodel.AttributeDefinition;

/**
 * @author M. Togna
 * 
 */
public class AttributeDefinitionProxy extends NodeDefinitionProxy implements ProxyBase {

	private transient AttributeDefinition attributeDefinition;

	public AttributeDefinitionProxy(AttributeDefinition attributeDefinition) {
		super(attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	public List<AttributeDefaultProxy> getAttributeDefaults() {
		return AttributeDefaultProxy.fromList(attributeDefinition.getAttributeDefaults());
	}

}
