/**
 * 
 */
package org.openforis.collect.model;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.NumericCode;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class LenientNumericCode extends NumericCode implements LenientValue {

	private String stringValue;

	public LenientNumericCode(String stringValue, String qualifier) {
		super(parse(stringValue), qualifier);
		this.stringValue = stringValue;
	}

	public LenientNumericCode(String stringValue) {
		super(parse(stringValue));
		this.stringValue = stringValue;
	}

	 @Override
	 public boolean isValid() {
		 return getCode()!=null;
	 }

	@Override
	public boolean isBlank() {
		return StringUtils.isBlank(stringValue) && StringUtils.isBlank(getQualifier());
	}

	private static Integer parse(String stringValue) {
		try {
			return new Integer(stringValue);
		} catch (NumberFormatException e) { 
			return null;
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
		result = prime * result
				+ ((getQualifier() == null) ? 0 : getQualifier().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LenientNumericCode other = (LenientNumericCode) obj;
		if (stringValue == null) {
			if (other.stringValue != null)
				return false;
		} else if (!stringValue.equals(other.stringValue))
			return false;
		if (getQualifier() == null) {
			if (other.getQualifier() != null)
				return false;
		} else if (!getQualifier().equals(other.getQualifier()))
			return false;
		return true;
	}

	@Override
	public String[] getStringValues() {
		return new String[] {stringValue, getQualifier()};
	}
}
