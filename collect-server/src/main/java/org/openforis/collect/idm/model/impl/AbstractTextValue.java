/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.model.TextValue;

/**
 * @author M. Togna
 * 
 */
public abstract class AbstractTextValue extends AbstractValue implements TextValue {

	public AbstractTextValue(String stringValue) {
		super(stringValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.TextValue#getString()
	 */
	@Override
	public String getString() {
		return this.getText1();
	}

	@Override
	public boolean isFormatValid() {
		return true;
	}

}
