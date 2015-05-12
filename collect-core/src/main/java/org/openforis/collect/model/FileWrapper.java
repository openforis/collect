package org.openforis.collect.model;

/**
 * 
 * @author S. Ricci
 *
 */
public class FileWrapper {

	private byte[] content;
	private String fileName;
	
	public FileWrapper(byte[] content, String fileName) {
		super();
		this.content = content;
		this.fileName = fileName;
	}
	
	public byte[] getContent() {
		return content;
	}
	
	public String getFileName() {
		return fileName;
	}
	
}
