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

	public FileAttributeDefinitionProxy(FileAttributeDefinition attributeDefinition) {
		super(attributeDefinition);
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
