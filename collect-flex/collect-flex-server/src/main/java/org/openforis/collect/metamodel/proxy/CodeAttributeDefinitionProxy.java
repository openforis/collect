/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.CodeAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient CodeAttributeDefinition attributeDefinition;

	public CodeAttributeDefinitionProxy(EntityDefinitionProxy parent, CodeAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	@ExternalizedProperty
	public String getListName() {
		return attributeDefinition.getListName();
	}

	@ExternalizedProperty
	public boolean isKey() {
		return attributeDefinition.isKey();
	}

	@ExternalizedProperty
	public boolean isAllowUnlisted() {
		return attributeDefinition.isAllowUnlisted();
	}

}
