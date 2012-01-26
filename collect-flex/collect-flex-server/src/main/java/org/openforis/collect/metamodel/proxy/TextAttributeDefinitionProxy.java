/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.TextAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class TextAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient TextAttributeDefinition attributeDefinition;

	public enum Type {
		SHORT, MEMO
	}

	public TextAttributeDefinitionProxy(TextAttributeDefinition attributeDefinition) {
		super(attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	@ExternalizedProperty
	public Type getType() {
		if (attributeDefinition.getType() != null) {
			return Type.valueOf(attributeDefinition.getType().toString());
		} else {
			return null;
		}
	}
	
	
}
