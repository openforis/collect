/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.model.FileValue;

/**
 * @author M. Togna
 * 
 */
public class FileValueImpl extends AbstractValue implements FileValue {

	private Long fileSize;

	public FileValueImpl(String fileName) {
		super(fileName);
	}

	public FileValueImpl(String fileName, Long fileSize) {
		super(fileName);
		this.fileSize = fileSize;
		this.setText2(String.valueOf(this.fileSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.FileValue#getFilename()
	 */
	@Override
	public String getFilename() {
		return this.getText1();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.FileValue#getSize()
	 */
	@Override
	public Long getSize() {
		return this.fileSize;
	}

	@Override
	public boolean isFormatValid() {
		return true;
	}

}
