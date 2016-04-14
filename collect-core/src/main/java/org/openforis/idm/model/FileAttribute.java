package org.openforis.idm.model;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.FileAttributeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class FileAttribute extends Attribute<FileAttributeDefinition, File> {

	private static final long serialVersionUID = 1L;
	
	private static final String OLD_FILE_NAME_FIELD_NAME = "fileName";
	private static final String OLD_FILE_SIZE_FIELD_NAME = "fileSize";

	public FileAttribute(FileAttributeDefinition definition) {
		super(definition);
	}
	
	@SuppressWarnings("unchecked")
	public Field<String> getFilenameField() {
		return (Field<String>) getField(0);
	}
	
	@SuppressWarnings("unchecked")
	public Field<Long> getSizeField() {
		return (Field<Long>) getField(1);
	}
	
	public String getFilename() {
		return getFilenameField().getValue();
	}
	
	public void setFilename(String filename) {
		getFilenameField().setValue(filename);
	}

	public Long getSize() {
		return getSizeField().getValue();
	}
	
	public void setSize(Long size) {
		getSizeField().setValue(size);
	}
	
	@Override
	public File getValue() {
		String filename = getFilenameField().getValue();
		Long size = getSizeField().getValue();
		return new File(filename, size);
	}

	@Override
	protected void setValueInFields(File value) {
		String filename = value.getFilename();
		Long size = value.getSize();
		getFilenameField().setValue(filename);
		getSizeField().setValue(size);
	}
	
	@Override
	public boolean isEmpty() {
		File f = getValue();
		return f == null || (f.getSize() == null && StringUtils.isBlank(f.getFilename()));
	}
	
	@Override
	public Field<?> getField(String name) {
		//backwards compatibility
		if ( OLD_FILE_NAME_FIELD_NAME.equals(name)) {
			return getFilenameField();
		} else if ( OLD_FILE_SIZE_FIELD_NAME.equals(name) ) {
			return getSizeField();
		} else {
			return super.getField(name);
		}
	}
	
}
