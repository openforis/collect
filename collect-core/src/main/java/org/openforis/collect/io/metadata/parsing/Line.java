package org.openforis.collect.io.metadata.parsing;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class Line {
	
	private long lineNumber;

	public long getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(long lineNumber) {
		this.lineNumber = lineNumber;
	}
	
}