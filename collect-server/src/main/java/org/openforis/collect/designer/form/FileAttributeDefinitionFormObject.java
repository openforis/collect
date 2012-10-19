/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.FileAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class FileAttributeDefinitionFormObject<T extends FileAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private static final String EXTENSIONS_SEPARATOR = ",";
	private int maxSize;
	private String extensions;
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setMaxSize(maxSize);
		dest.removeAllExtensions();
		if  ( extensions != null ) {
			String[] extensionsArr = extensions.split(EXTENSIONS_SEPARATOR);
			List<String> extensionsList = Arrays.asList(extensionsArr);
			dest.addExtensions(extensionsList);
		}
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		maxSize = source.getMaxSize();
		List<String> extensionsList = source.getExtensions();
		extensions = StringUtils.join(extensionsList, EXTENSIONS_SEPARATOR);
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public String getExtensions() {
		return extensions;
	}

	public void setExtensions(String extensions) {
		this.extensions = extensions;
	}

}
