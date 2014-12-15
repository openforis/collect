/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class BooleanAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient BooleanAttributeDefinition attributeDefinition;
	
	public BooleanAttributeDefinitionProxy(EntityDefinitionProxy parent, BooleanAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	@ExternalizedProperty
	public boolean isAffirmativeOnly() {
		return attributeDefinition.isAffirmativeOnly();
	}

	
}
