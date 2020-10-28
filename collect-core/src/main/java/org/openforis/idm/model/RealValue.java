package org.openforis.idm.model;

/**
 * 
 * @author G. Miceli
 *
 */
public class RealValue extends NumberValue<Double> {

	public RealValue(Double value) {
		super(value);
	}

	public RealValue(Double value, Integer unitId) {
		super(value, unitId);
	}

}
