/**
 * 
 */
package org.openforis.collect.remoting.service;

/**
 * @author S. Ricci
 *
 */
public class FileWrapper {
	
	private String fileName;
	private byte[] data;
	private String contentType;

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
	
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
