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

	BLANK_ON_FORM('B'), 
	DASH_ON_FORM('D'), 
	ILLEGIBLE('I');

	private final Character code;

	private FieldSymbol(Character code) {
		this.code = code;
	}

	public char getCode() {
		return code;
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

	public boolean isReasonBlank() {
		return this == BLANK_ON_FORM || this == DASH_ON_FORM || this == ILLEGIBLE;
	}
}
