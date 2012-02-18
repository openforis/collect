/**
 * 
 */
package org.openforis.collect.model.proxy;

/**
 * 
 * @author S. Ricci
 *
 */
public enum AttributeSymbol {

	BLANK_ON_FORM('B', '*'),
	DASH_ON_FORM('D', '-'),
	ILLEGIBLE('I', '?');
	
	private final Character code;
	private final Character shortKey;

	private AttributeSymbol(Character code) {
		this.code = code;
		this.shortKey = null;
	}
	
	private AttributeSymbol(Character code, Character shortKey) {
		this.code = code;
		this.shortKey = shortKey;
	}

	public char getCode() {
		return code;
	}
	
	public Character getShortKey() {
		return shortKey;
	}
	
	public static AttributeSymbol valueOf(Character code) {
		if(code != null) {
			for (AttributeSymbol value : values()) {
				if(value.getCode() == code) {
					return value;
				}
			}
		}
		return null;
	}
	
	public static AttributeSymbol fromShortKey(String shortKey) {
		char c = shortKey.charAt(0);
		return fromShortKey(c);
	}
	
	public static AttributeSymbol fromShortKey(Character shortKey) {
		for (AttributeSymbol value : values()) {
			if(value.getShortKey() == shortKey) {
				return value;
			}
		}
		return null;
	}

	public static boolean isShortKeyForBlank(String value) {
		if(value != null && value.length() == 1) {
			char shortKey = value.charAt(0);
			AttributeSymbol symbol = fromShortKey(shortKey);
			return symbol != null;
		}
		return false;
	}
	
	public static boolean isShortKeyForBlank(String[] values) {
		if(values != null && values.length == 1) {
			return isShortKeyForBlank(values[0]);
		}
		return false;
	}
	
	public boolean isReasonBlank() {
		return this == BLANK_ON_FORM || this == DASH_ON_FORM || this == ILLEGIBLE;
	}
}
