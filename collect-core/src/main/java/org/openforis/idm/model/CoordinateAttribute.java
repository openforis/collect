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

	@Override
	public Coordinate getValue() {
		Double x = getXField().getValue();
		Double y = getYField().getValue();
		String srsId = getSrsIdField().getValue();
		return new Coordinate(x, y, srsId);
	}

	@Override
	protected void setValueInFields(Coordinate value) {
		Double x = value.getX();
		Double y = value.getY();
		String srsId = value.getSrsId();
		getXField().setValue(x);
		getYField().setValue(y);
		getSrsIdField().setValue(srsId);
	}
	
	@Override
	public String extractTextValue() {
		Coordinate coordinate = getValue();
		return coordinate != null && coordinate.isComplete() ? coordinate.toString() : null;
	}
}
