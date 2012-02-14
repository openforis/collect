package org.openforis.collect.metamodel.proxy;

import org.openforis.idm.metamodel.CoordinateAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class CoordinateAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient CoordinateAttributeDefinition attributeDefinition;
	
	public CoordinateAttributeDefinitionProxy(EntityDefinitionProxy parent, CoordinateAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	
}
