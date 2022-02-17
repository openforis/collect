package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations.FileType;
import org.openforis.idm.metamodel.AttributeType;

public class FileAttributeDefView extends AttributeDefView {

	private FileType fileType;
	private Integer maxSize;
	private List<String> extensions;

	public FileAttributeDefView(int id, String name, String label, AttributeType type, List<String> fieldNames,
			boolean key, boolean multiple) {
		super(id, name, label, type, fieldNames, key, multiple);
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public Integer getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

	public List<String> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<String> extensions) {
		this.extensions = extensions;
	}

}
