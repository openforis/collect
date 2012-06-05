package org.openforis.collect.model;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.BooleanValue;


/**
 * @author G. Miceli
 * @author M. Togna
 */
public class BooleanValueImpl extends BooleanValue implements LenientValue {

	private String stringValue;

	public BooleanValueImpl(String stringValue) {
		super(parse(stringValue));
		this.stringValue = stringValue;
	}

	private static Boolean parse(String stringValue) {
		try {
			return new Boolean(stringValue);
		} catch (NumberFormatException e) { 
			return null;
		}
	}

	@Override
	public boolean isBlank() {
		return StringUtils.isBlank(stringValue);
	}

	@Override
	public boolean isValid() {
		return getBoolean() != null;
	}
	
	@Override
	public String[] getStringValues() {
		return new String[] {stringValue};
	}
}
