/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.Value;

/**
 * @author M. Togna
 * 
 */
public abstract class AbstractValue implements Value {

	public static enum Symbol {
		BLANK_ON_FORM('*'), DASH_ON_FORM('-'), ILLEGIBLE('?');

		private Character symbol;

		private Symbol(Character symbol) {
			this.symbol = symbol;
		}

		public boolean equals(CharSequence charSequence) {
			if (charSequence.length() == 1) {
				Character other = charSequence.charAt(0);
				return this.symbol.equals(other);
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return this.symbol.toString();
		}

		public static Symbol fromString(String string) {
			if (string.length() == 1) {
				Character character = string.charAt(0);
				for (Symbol symbol : values()) {
					if (symbol.equals(character))
						return symbol;
				}
			}
			return null;
		}
	}

	private String value1;
	private String value2;
	private String value3;
	private String value4;
	private Symbol symbol;

	// public AbstractValue() {
	// super();
	// }

	public AbstractValue(String stringValue) {
		this.symbol = Symbol.fromString(stringValue);
		if (this.symbol == null) {
			this.value1 = stringValue;
		}
	}

	@Override
	public boolean isBlank() {
		return StringUtils.isBlank(this.value1);
	}

	protected String getValue1() {
		return this.value1;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	/**
	 * @return the value2
	 */
	protected String getValue2() {
		return value2;
	}

	/**
	 * @return the value3
	 */
	protected String getValue3() {
		return value3;
	}

	/**
	 * @return the value4
	 */
	protected String getValue4() {
		return value4;
	}

}
