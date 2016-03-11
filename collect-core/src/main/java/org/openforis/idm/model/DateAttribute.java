package org.openforis.idm.model;

import org.openforis.idm.metamodel.DateAttributeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class DateAttribute extends Attribute<DateAttributeDefinition, Date> {

	private static final long serialVersionUID = 1L;

	public DateAttribute(DateAttributeDefinition definition) {
		super(definition);
	}

	@SuppressWarnings("unchecked")
	public Field<Integer> getYearField() {
		return (Field<Integer>) getField(0);
	}

	@SuppressWarnings("unchecked")
	public Field<Integer> getMonthField() {
		return (Field<Integer>) getField(1);
	}

	@SuppressWarnings("unchecked")
	public Field<Integer> getDayField() {
		return (Field<Integer>) getField(2);
	}

	@Override
	public Date getValue() {
		Integer year = getYearField().getValue();
		Integer month = getMonthField().getValue();
		Integer day = getDayField().getValue();
		return new Date(year, month, day);
	}
	
	@Override
	protected void setValueInFields(Date value) {
		Integer year = value.getYear();
		Integer month = value.getMonth();
		Integer day = value.getDay();
		getYearField().setValue(year);
		getMonthField().setValue(month);
		getDayField().setValue(day);
	}
	
}
