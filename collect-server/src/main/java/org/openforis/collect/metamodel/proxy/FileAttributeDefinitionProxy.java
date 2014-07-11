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

	private static final String[] IMAGE_CONTENT_EXTENSIONS = new String[] {"jpg", "jpeg", "png", "bmp"};
	
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
	
	@ExternalizedProperty
	public boolean isImageContent() {
		return containsOne(attributeDefinition.getExtensions(), IMAGE_CONTENT_EXTENSIONS, true);
	}
	
	protected static boolean containsOne(List<String> list, String[] values, boolean caseSensitive) {
		for (String value : values) {
			if ( contains(list, value, caseSensitive) ) {
				return true;
			}
		}
		return false;
	}
		
	protected static boolean contains(List<String> list, String value, boolean caseSensitive) {
		if ( caseSensitive ) {
			return list.contains(value);
		} else {
			for (String item : list) {
				if ( item.equalsIgnoreCase(value) ) {
					return true;
				}
			}
			return false;
		}
	}
	
}
