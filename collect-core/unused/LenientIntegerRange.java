/**
 * 
 */
package org.openforis.collect.model;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.IntegerRange;

/**
 * @author M. Togna
 * @author G. Miceli
 * 
 */
public class LenientIntegerRange extends IntegerRange implements LenientValue {

	private String stringValue;

	public LenientIntegerRange(String stringValue) {
		super(parseFrom(stringValue), parseTo(stringValue));
		this.stringValue = stringValue;
	}

	private static Integer parseFrom(String stringValue) {
		return parse(stringValue, 0);
	}

	private static Integer parseTo(String stringValue) {
		return parse(stringValue, 1);
	}

	private static Integer parse(String stringValue, int part) {
		if (stringValue != null) {
			String[] parts = stringValue.split("-");
			String s;
			if (parts.length == 1 || parts.length == 2) {
				if (parts.length == 1) {
					s = parts[0];
				} else {
					s = parts[part];
				}
				try {
					return new Integer(s);
				} catch (NumberFormatException nfe) {
				}
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((stringValue == null) ? 0 : stringValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LenientIntegerRange other = (LenientIntegerRange) obj;
		if (stringValue == null) {
			if (other.stringValue != null)
				return false;
		} else if (!stringValue.equals(other.stringValue))
			return false;
		return true;
	}

	@Override
	public boolean isBlank() {
		return !StringUtils.isBlank(stringValue);
	}

	@Override
	public boolean isValid() {
		return getFrom() != null && getTo() != null;
	}

	@Override
	public String[] getStringValues() {
		return new String[] {stringValue};
	}
}
