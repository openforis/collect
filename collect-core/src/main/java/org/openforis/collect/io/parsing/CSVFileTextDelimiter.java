package org.openforis.collect.io.parsing;

/**
 * 
 * @author S. Ricci
 *
 */
public enum CSVFileTextDelimiter {

	DOUBLE_QUOTE('"'),
	SINGLE_QUOTE('\''); 
	
	private char delimiter;

	CSVFileTextDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}
	
	public char getCharacter() {
		return delimiter;
	}
}
