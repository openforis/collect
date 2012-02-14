package org.openforis.collect.metamodel.proxy;

import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class TimeAttributeDefinitionProxy extends AttributeDefinitionProxy {
	
	private transient TimeAttributeDefinition attributeDefinition;
	
	public TimeAttributeDefinitionProxy(EntityDefinitionProxy parent, TimeAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}
	
}
