package org.openforis.idm.model;

/**
 * 
 * @author G. Miceli
 *
 */
public class IntegerValue extends NumberValue<Integer> {

	public IntegerValue(Integer value) {
		this(value, null);
	}
	
	public IntegerValue(Integer value, Integer unitId) {
		super(value, unitId);
	}
	
}
