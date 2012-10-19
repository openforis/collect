package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.FileAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class FileAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient FileAttributeDefinition attributeDefinition;

	public FileAttributeDefinitionProxy(EntityDefinitionProxy parent, FileAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	@ExternalizedProperty
	public Integer getMaxSize() {
		return attributeDefinition.getMaxSize();
	}

	@ExternalizedProperty
	public List<String> getExtensions() {
		return attributeDefinition.getExtensions();
	}
	
}
