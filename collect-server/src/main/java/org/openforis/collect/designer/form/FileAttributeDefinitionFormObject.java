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
	
	private static final int MB_IN_BYTES = 1024 * 1024;

	private static final String EXTENSIONS_SEPARATOR = " ";
	
	/**
	* Max file size in mega bytes
	*/
	private Integer maxSize;
	private String extensions;
	
	FileAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setMaxSize(convertToBytes(maxSize));
		dest.removeAllExtensions();
		if  ( StringUtils.isNotBlank(extensions) ) {
			String[] extensionsArr = extensions.split(EXTENSIONS_SEPARATOR);
			List<String> extensionsList = Arrays.asList(extensionsArr);
			dest.addExtensions(extensionsList);
		}
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		maxSize = convertToMB(source.getMaxSize());
		List<String> extensionsList = source.getExtensions();
		extensions = StringUtils.join(extensionsList, EXTENSIONS_SEPARATOR);
	}

	private static Integer convertToMB(Integer bytes) {
		if ( bytes == null ) {
			return null;
		} else {
			double part = (double) (bytes / (double) MB_IN_BYTES);
			double result = Math.ceil(part);
			return Double.valueOf(result).intValue();
		}
	}
	
	private static Integer convertToBytes(Integer megaBytes) {
		if ( megaBytes == null ) {
			return null;
		} else {
			int result = megaBytes * MB_IN_BYTES;
			return Double.valueOf(result).intValue();
		}
	}
	
	public String getExtensions() {
		return extensions;
	}

	public void setExtensions(String extensions) {
		this.extensions = extensions;
	}
	
	public Integer getMaxSize() {
		return maxSize;
	}
	
	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

}
