package org.openforis.collect.metamodel.proxy;

import org.openforis.idm.metamodel.DateAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class DateAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient DateAttributeDefinition attributeDefinition;

	public DateAttributeDefinitionProxy(EntityDefinitionProxy parent, DateAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}
	
}
