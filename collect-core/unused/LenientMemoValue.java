/**
 * 
 */
package org.openforis.collect.model;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.MemoValue;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class LenientMemoValue extends MemoValue implements LenientValue {

	public LenientMemoValue(String stringValue) {
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
