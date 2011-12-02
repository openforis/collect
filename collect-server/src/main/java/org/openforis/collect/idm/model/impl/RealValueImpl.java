/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.model.RealValue;

/**
 * @author M. Togna
 * 
 */
public class RealValueImpl extends AbstractNumericValue<Double> implements RealValue {

	public RealValueImpl(String stringValue) {
		super(stringValue);
	}

	@Override
	public Double getNumber() {
		try {
			return Double.parseDouble(getValue1());
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
