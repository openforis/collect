/**
 * 
 */
package org.openforis.collect.model;

/**
 * 
 * @author S. Ricci
 * 
 */
// TODO check name: FieldSymbol? AttributeFieldSymbol?
public enum FieldSymbol {

	// TODO Symbol for confirmed value
	BLANK_ON_FORM('B', '*'), 
	DASH_ON_FORM('D', '-'), 
	ILLEGIBLE('I', '?'),
	CONFIRMED('C', 'C');

	private final Character code;
	// TODO check for a better name
	private final Character symbol;

	private FieldSymbol(Character code) {
		this.code = code;
		this.symbol = null;
	}

	private FieldSymbol(Character code, Character shortKey) {
		this.code = code;
		this.symbol = shortKey;
	}

	public char getCode() {
		return code;
	}

	public Character getSymbol() {
		return symbol;
	}

	public static FieldSymbol valueOf(Character code) {
		if (code != null) {
			for (FieldSymbol value : values()) {
				if (value.getCode() == code) {
					return value;
				}
			}
		}
		return null;
	}

	public static boolean isValidCode(Character code) {
		FieldSymbol fieldSymbol = valueOf(code);
		return fieldSymbol != null;
	}

	public static FieldSymbol fromCharacterSymbol(String charSymbol) {
		char c = charSymbol.charAt(0);
		return fromCharacterSymbol(c);
	}

	public static FieldSymbol fromCharacterSymbol(Character charSymbol) {
		for (FieldSymbol value : values()) {
			if (value.getSymbol() == charSymbol) {
				return value;
			}
		}
		return null;
	}

	public static boolean isShortKeyForBlank(String value) {
		if (value != null && value.length() == 1) {
			char shortKey = value.charAt(0);
			FieldSymbol symbol = fromCharacterSymbol(shortKey);
			return symbol != null;
		}
		return false;
	}

	public static boolean isShortKeyForBlank(String[] values) {
		if (values != null && values.length == 1) {
			return isShortKeyForBlank(values[0]);
		}
		return false;
	}

	public boolean isReasonBlank() {
		return this == BLANK_ON_FORM || this == DASH_ON_FORM || this == ILLEGIBLE;
	}
}
