package org.openforis.idm.model;

import org.openforis.idm.metamodel.CoordinateAttributeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CoordinateAttribute extends Attribute<CoordinateAttributeDefinition, Coordinate> {

	private static final long serialVersionUID = 1L;

	public CoordinateAttribute(CoordinateAttributeDefinition definition) {
		super(definition);
	}

	@SuppressWarnings("unchecked")
	public Field<Double> getXField() {
		return (Field<Double>) getField(0);
	}

	@SuppressWarnings("unchecked")
	public Field<Double> getYField() {
		return (Field<Double>) getField(1);
	}
	
	@SuppressWarnings("unchecked")
	public Field<String> getSrsIdField() {
		return (Field<String>) getField(2);
	}
	
	@SuppressWarnings("unchecked")
	public Field<Double> getAltitudeField() {
		return (Field<Double>) getField(3);
	}

	@SuppressWarnings("unchecked")
	public Field<Double> getAccuracyField() {
		return (Field<Double>) getField(4);
	}

	@Override
	public Coordinate getValue() {
		Double x = getXField().getValue();
		Double y = getYField().getValue();
		String srsId = getSrsIdField().getValue();
		Double altitude = getAltitudeField().getValue();
		Double accuracy = getAccuracyField().getValue();
		
		return new Coordinate(x, y, srsId, altitude, accuracy);
	}

	@Override
	protected void setValueInFields(Coordinate value) {
		getXField().setValue(value.getX());
		getYField().setValue(value.getY());
		getSrsIdField().setValue(value.getSrsId());
		getAltitudeField().setValue(value.getAltitude());
		getAccuracyField().setValue(value.getAccuracy());
	}
	
}
