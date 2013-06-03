/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.model.Field;

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

	public static boolean isReasonBlankSpecified(Field<?> field) {
		Character symbol = field.getSymbol();
		if ( symbol == null ) {
			return false;
		} else {			
			FieldSymbol fieldSymbol = FieldSymbol.valueOf(symbol);
			return fieldSymbol == null ? false: fieldSymbol.isReasonBlank();
		}
	}

	public boolean isReasonBlank() {
		return this == BLANK_ON_FORM || this == DASH_ON_FORM || this == ILLEGIBLE;
	}
	
}
