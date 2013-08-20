package org.openforis.collect.model;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public enum ValueState {
	CONFIRMED('C'), UNAVAILABLE('U');

	private char symbol;

	private ValueState(char symbol) {
		this.symbol = symbol;
	}

	public char getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return String.valueOf(symbol);
	}

	public static ValueState fromString(String string) {
		if (string != null && string.length() == 1) {
			char character = string.charAt(0);
			for (ValueState value : values()) {
				if (value.symbol == character) {
					return value;
				}
			}
		}
		return null;
	}
}
