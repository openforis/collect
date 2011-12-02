/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.model.NumericValue;

/**
 * @author M. Togna
 * 
 */
public abstract class AbstractNumericValue<T extends Number> extends AbstractValue implements NumericValue<T> {

	public AbstractNumericValue(String stringValue) {
		super(stringValue);
	}

}
