/**
 * 
 */
package org.openforis.collect.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
			if (string != null && string.length() == 1) {
				Character character = string.charAt(0);
				for (Symbol symbol : values()) {
					if (symbol.equals(character)) {
						return symbol;
					}
				}
			}
			return null;
		}
	}

	private String text1;
	private String text2;
	private String text3;
	private String text4;

	private String remarks;
	private boolean approved;

	private Symbol symbol;

	// public AbstractValue() {
	// super();
	// }

	public AbstractValue(String stringValue) {
		this.text1 = stringValue;
		this.symbol = Symbol.fromString(stringValue);
	}

	public boolean isBlank() {
		return StringUtils.isBlank(this.text1);
	}

	public abstract boolean isFormatValid();

	protected String getText1() {
		return this.text1;
	}

	public Symbol getSymbol() {
		return this.symbol;
	}

	/**
	 * @return the text2
	 */
	protected String getText2() {
		return this.text2;
	}

	/**
	 * @return the text3
	 */
	protected String getText3() {
		return this.text3;
	}

	/**
	 * @param text1
	 *            the text1 to set
	 */
	protected void setText1(String value1) {
		this.text1 = value1;
	}

	/**
	 * @param text2
	 *            the text2 to set
	 */
	protected void setText2(String value2) {
		this.text2 = value2;
	}

	/**
	 * @param text3
	 *            the text3 to set
	 */
	protected void setText3(String value3) {
		this.text3 = value3;
	}

	/**
	 * @return the remarks
	 */
	protected String getRemarks() {
		return remarks;
	}

	/**
	 * @return the approved
	 */
	protected boolean isApproved() {
		return approved;
	}

	public String getText4() {
		return text4;
	}

	protected void setText4(String text4) {
		this.text4 = text4;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.text1).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractValue other = (AbstractValue) obj;
		return new EqualsBuilder().append(this.text1, other.text1).isEquals();
	}

}
