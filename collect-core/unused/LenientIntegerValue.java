/**
 * 
 */
package org.openforis.collect.model;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.IntegerValue;

/**
 * @author M. Togna
 * @author G. Miceli
 * 
 */
public class LenientIntegerValue extends IntegerValue implements LenientValue {

	private String stringValue;

	public LenientIntegerValue(String stringValue) {
		super(parse(stringValue));
		this.stringValue = stringValue;
	}

	@Override
	public int hashCode() {
		return stringValue==null ? 0 : stringValue.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LenientIntegerValue other = (LenientIntegerValue) obj;
		if (stringValue == null) {
			if (other.stringValue != null)
				return false;
		} else if (!stringValue.equals(other.stringValue))
			return false;
		return true;
	}

	private static Integer parse(String stringValue) {
		if (StringUtils.isBlank(stringValue)) {
			return null;
		} else {
			try {
				return Integer.parseInt(stringValue);
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}

	@Override
	public boolean isBlank() {
		return StringUtils.isBlank(stringValue);
	}

	@Override
	public boolean isValid() {
		return getNumber()!=null;
	}

	@Override
	public String[] getStringValues() {
		return new String[] {stringValue};
	}
}
