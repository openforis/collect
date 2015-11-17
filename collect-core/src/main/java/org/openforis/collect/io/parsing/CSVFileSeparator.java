package org.openforis.collect.io.parsing;

/**
 * 
 * @author S. Ricci
 *
 */
public enum CSVFileSeparator {

	COMMA(','), SEMICOLON(';');
	
	private char separator;

	CSVFileSeparator(char separator) {
		this.separator = separator;
	}
	
	public char getCharacter() {
		return separator;
	}
}
