/**
 * 
 */
package org.openforis.collect.model;

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
			return Double.parseDouble(this.getText1());
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
