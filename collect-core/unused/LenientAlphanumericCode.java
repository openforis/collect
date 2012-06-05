/**
 * 
 */
package org.openforis.collect.model;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.AlphanumericCode;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class LenientAlphanumericCode extends AlphanumericCode implements LenientValue {

	public LenientAlphanumericCode(String value, String qualifier) {
		super(value, qualifier);
	}

	public LenientAlphanumericCode(String stringValue) {
		super(stringValue);
	}

	@Override
	public boolean isBlank() {
		return StringUtils.isBlank(getCode()) && StringUtils.isBlank(getQualifier());
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String[] getStringValues() {
		return new String[] {getCode(), getQualifier()};
	}
}
