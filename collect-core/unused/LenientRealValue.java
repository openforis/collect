/**
 * 
 */
package org.openforis.collect.model;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.RealValue;

/**
 * @author M. Togna
 * @author G. Miceli
 * 
 */
public class LenientRealValue extends RealValue implements LenientValue {

	private String stringValue;

	public LenientRealValue(String stringValue) {
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
		LenientRealValue other = (LenientRealValue) obj;
		if (stringValue == null) {
			if (other.stringValue != null)
				return false;
		} else if (!stringValue.equals(other.stringValue))
			return false;
		return true;
	}

	private static Double parse(String stringValue) {
		if (StringUtils.isBlank(stringValue)) {
			return null;
		} else {
			try {
				return new Double(stringValue);
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
