package org.openforis.idm.model;

import org.openforis.idm.metamodel.Unit;

/**
 * 
 * @author G. Miceli
 *
 */
public class RealValue extends NumberValue<Double> {

	public RealValue(Double value, Unit unit) {
		super(value, unit);
	}
}
