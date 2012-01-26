package org.openforis.collect.metamodel.proxy;

import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class TimeAttributeDefinitionProxy extends AttributeDefinitionProxy {
	
	private transient TimeAttributeDefinition attributeDefinition;
	
	public TimeAttributeDefinitionProxy(TimeAttributeDefinition attributeDefinition) {
		super(attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}
	
}
