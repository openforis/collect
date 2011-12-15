package org.openforis.collect.model;

import org.openforis.idm.model.BooleanValue;

/**
 * 
 * @author M. Togna
 * 
 */
public class BooleanValueImpl extends AbstractValue implements BooleanValue {

	private static final String TRUE = "true";
	private static final String FALSE = "false";

	public BooleanValueImpl(String stringValue) {
		super(stringValue);
	}

	@Override
	public Boolean getBoolean() {
		if (!this.isBlank()) {
			if (TRUE.equals(this.getText1())) {
				return Boolean.TRUE;
			} else if (FALSE.equals(this.getText1())) {
				return Boolean.FALSE;
			}
		}
		return null;
	}

	@Override
	public boolean isFormatValid() {
		return getBoolean() != null;
	}

}
