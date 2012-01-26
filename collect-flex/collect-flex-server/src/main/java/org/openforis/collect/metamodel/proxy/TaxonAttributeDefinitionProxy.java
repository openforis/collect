/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.openforis.idm.metamodel.TaxonAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class TaxonAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient TaxonAttributeDefinition attributeDefinition;
	
	public TaxonAttributeDefinitionProxy(TaxonAttributeDefinition attributeDefinition) {
		super(attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}
	
	
}
