/**
 * 
 */
package org.openforis.collect.remoting.service;

/**
 * @author S. Ricci
 *
 */
public class FileWrapper {
	
	private byte[] data;
	
	private String fileName;

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
