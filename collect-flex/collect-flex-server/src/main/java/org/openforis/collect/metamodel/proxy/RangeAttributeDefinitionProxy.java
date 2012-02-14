/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.openforis.idm.metamodel.RangeAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class RangeAttributeDefinitionProxy extends NumericAttributeDefinitionProxy {
	
	private transient RangeAttributeDefinition attributeDefinition;

	public RangeAttributeDefinitionProxy(EntityDefinitionProxy parent, RangeAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}
	
}
