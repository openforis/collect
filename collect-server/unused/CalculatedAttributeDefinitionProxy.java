/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.openforis.idm.metamodel.CalculatedAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class CalculatedAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient CalculatedAttributeDefinition attributeDefinition;

	public CalculatedAttributeDefinitionProxy(EntityDefinitionProxy parent, CalculatedAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

}
