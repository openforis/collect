package org.openforis.idm.model;

import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Unit;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class NumberAttribute<N extends Number, T extends NumberValue<N>> extends Attribute<NumberAttributeDefinition, T> {

	private static final long serialVersionUID = 1L;

	protected NumberAttribute(NumberAttributeDefinition definition) {
		super(definition);
	}
	
	public Integer getUnitId() {
		return getUnitField().getValue();
	}
	
	public String getUnitName() {
		return getUnitNameField().getValue();
	}

	public Unit getUnit() {
		Integer unitId = getUnitId();
		Unit unit = null;
		if ( unitId != null ) {
			unit = getSurvey().getUnit(unitId);
		} else {
			String unitName = getUnitName();
			if ( unitName != null ) {
				unit = getSurvey().getUnit(unitName);
			}
		}
		return unit;
	}

	public void setUnit(Unit unit) {
		Integer unitId = unit == null ? null: unit.getId();
		setUnitId(unitId);
	}
	
	@SuppressWarnings("unchecked")
	public Field<N> getNumberField() {
		return (Field<N>) getField(0);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getUnitNameField() {
		return (Field<String>) getField(1);
	}

	@SuppressWarnings("unchecked")
	public Field<Integer> getUnitField() {
		return (Field<Integer>) getField(2);
	}

	public N getNumber() {
		return getNumberField().getValue();
	}
	
	public void setNumber(N value) {
		getNumberField().setValue(value);
	}
	
	public void setUnitName(String name) {
		getUnitNameField().setValue(name);
	}
	
	public void setUnitId(Integer id) {
		getUnitField().setValue(id);
	}
	
	protected abstract T createValue(N value, Unit unit);
	
	@Override
	protected void setValueInFields(T value) {
		N number = value.getValue();
		Integer unitId = value.getUnitId();
		getNumberField().setValue(number);
		getUnitField().setValue(unitId);
	}

	@Override
	public T getValue() {
		N value = (N) getNumberField().getValue();
		Unit unit = getUnit();
		return createValue(value, unit);
	}
	
	@Override
	protected boolean calculateHasData() {
		return getNumberField().hasData();
	}

	@Override
	protected boolean calculateIsEmpty() {
		return ! getNumberField().hasValue();
	}
	
}
