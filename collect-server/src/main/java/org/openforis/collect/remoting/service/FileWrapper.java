/**
 * 
 */
package org.openforis.collect.remoting.service;

/**
 * @author S. Ricci
 *
 */
public class FileWrapper {
	
	private String filePath;
	private String originalFileName;
	private String contentType;

	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}
}
