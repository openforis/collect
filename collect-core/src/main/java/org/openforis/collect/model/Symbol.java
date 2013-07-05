package org.openforis.collect.model;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public enum Symbol {
	BLANK_ON_FORM('*'), DASH_ON_FORM('-'), ILLEGIBLE('?');

	private char shortCut;

	private Symbol(char shortCut) {
		this.shortCut = shortCut;
	}

	public char getShortCut() {
		return shortCut;
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.shortCut);
	}

	public static Symbol fromString(String string) {
		if (string != null && string.length() == 1) {
			char character = string.charAt(0);
			for (Symbol symbol : values()) {
				if (symbol.shortCut == character) {
					return symbol;
				}
			}
		}
		return null;
	}
}
