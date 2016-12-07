/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.CollectAnnotations.FileType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class FileAttributeDefinitionFormObject<T extends FileAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private static final int MB_IN_BYTES = 1024 * 1024;

	/**
	* Max file size in mega bytes
	*/
	private Integer maxSize;
	private String fileType;
	
	FileAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setMaxSize(convertToBytes(maxSize));
		dest.removeAllExtensions();
		CollectAnnotations annotations = ((CollectSurvey) dest.getSurvey()).getAnnotations();
		annotations.setFileType(dest, FileType.valueOf(fileType));
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		maxSize = convertToMB(source.getMaxSize());
		CollectAnnotations annotations = ((CollectSurvey) source.getSurvey()).getAnnotations();
		fileType = annotations.getFileType(source).name();
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
	
	public Integer getMaxSize() {
		return maxSize;
	}
	
	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}
	
	public String getFileType() {
		return fileType;
	}
	
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

}
