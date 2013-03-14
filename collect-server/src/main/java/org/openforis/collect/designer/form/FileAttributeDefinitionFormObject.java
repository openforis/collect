/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class FileAttributeDefinitionFormObject<T extends FileAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private static final String EXTENSIONS_SEPARATOR = " ";
	
	private Integer maxSize;
	private String extensions;
	
	FileAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setMaxSize(maxSize);
		dest.removeAllExtensions();
		if  ( StringUtils.isNotBlank(extensions) ) {
			String[] extensionsArr = extensions.split(EXTENSIONS_SEPARATOR);
			List<String> extensionsList = Arrays.asList(extensionsArr);
			dest.addExtensions(extensionsList);
		}
	}
	
	@Override
	public void loadFrom(T source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		maxSize = source.getMaxSize();
		List<String> extensionsList = source.getExtensions();
		extensions = StringUtils.join(extensionsList, EXTENSIONS_SEPARATOR);
	}

	public Integer getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

	public String getExtensions() {
		return extensions;
	}

	public void setExtensions(String extensions) {
		this.extensions = extensions;
	}

}
