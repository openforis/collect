/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.openforis.idm.metamodel.NumberAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class NumberAttributeDefinitionProxy extends NumericAttributeDefinitionProxy {

	public NumberAttributeDefinitionProxy(EntityDefinitionProxy parent, NumberAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
	}
	
}
