/**
 * 
 */
package org.openforis.collect.model;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.ShortTextValue;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class LenientShortTextValue extends ShortTextValue implements LenientValue {

	public LenientShortTextValue(String stringValue) {
		super(stringValue);
	}

	@Override
	public boolean isBlank() {
		return StringUtils.isBlank(getString());
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String[] getStringValues() {
		return new String[] {getString()};
	}
}
