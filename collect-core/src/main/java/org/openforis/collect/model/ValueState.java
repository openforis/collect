package org.openforis.collect.model;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public enum ValueState {
	CONFIRMED('C'), UNAVAILABLE('U');

	private Character symbol;

	private ValueState(Character symbol) {
		this.symbol = symbol;
	}

	public boolean equals(CharSequence charSequence) {
		if (charSequence.length() == 1) {
			Character other = charSequence.charAt(0);
			return this.symbol.equals(other);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.symbol.toString();
	}

	public static ValueState fromString(String string) {
		if (string != null && string.length() == 1) {
			Character character = string.charAt(0);
			for (ValueState symbol : values()) {
				if (symbol.equals(character)) {
					return symbol;
				}
			}
		}
		return null;
	}
}
