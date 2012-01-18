package org.openforis.collect.metamodel.proxy;

import org.openforis.idm.metamodel.DateAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class DateAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient DateAttributeDefinition attributeDefinition;

	public DateAttributeDefinitionProxy(DateAttributeDefinition attributeDefinition) {
		super(attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}
	
}
