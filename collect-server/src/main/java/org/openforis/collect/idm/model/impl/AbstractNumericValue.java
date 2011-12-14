/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.model.NumberValue;

/**
 * @author M. Togna
 * 
 */
public abstract class AbstractNumericValue<T extends Number> extends AbstractValue implements NumberValue<T> {

	public AbstractNumericValue(String stringValue) {
		super(stringValue);
	}

	@Override
	public boolean isFormatValid() {
		return getNumber() != null;
	}

}
