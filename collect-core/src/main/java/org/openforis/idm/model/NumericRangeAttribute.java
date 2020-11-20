package org.openforis.idm.model;

import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class NumericRangeAttribute<T extends NumericRange<N>, N extends Number>
		extends Attribute<RangeAttributeDefinition, T> {

	private static final long serialVersionUID = 1L;

	protected NumericRangeAttribute(RangeAttributeDefinition definition) {
		super(definition);
	}

	@SuppressWarnings("unchecked")
	public Field<N> getFromField() {
		return (Field<N>) getField(0);
	}

	@SuppressWarnings("unchecked")
	public Field<N> getToField() {
		return (Field<N>) getField(1);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getUnitNameField() {
		return (Field<String>) getField(2);
	}

	@SuppressWarnings("unchecked")
	public Field<Integer> getUnitField() {
		return (Field<Integer>) getField(3);
	}

	public N getFrom() {
		return getFromField().getValue();
	}

	public void setFrom(N value) {
		getFromField().setValue(value);
	}

	public N getTo() {
		return getToField().getValue();
	}

	public void setTo(N value) {
		getToField().setValue(value);
	}

	public String getUnitName() {
		return getUnitNameField().getValue();
	}

	public void setUnitName(String name) {
		getUnitNameField().setValue(name);
	}

	public Integer getUnitId() {
		return getUnitField().getValue();
	}

	public void setUnitId(Integer id) {
		getUnitField().setValue(id);
	}

	public Unit getUnit() {
		Integer unitId = getUnitId();
		Unit unit = null;
		if (unitId != null) {
			unit = getSurvey().getUnit(unitId);
		} else {
			String unitName = getUnitName();
			if (unitName != null) {
				unit = getSurvey().getUnit(unitName);
			}
		}
		return unit;
	}

	public void setUnit(Unit unit) {
		Integer unitId = unit == null ? null : unit.getId();
		setUnitId(unitId);
	}

	@Override
	public T getValue() {
		N from = getFromField().getValue();
		N to = getToField().getValue();
		Integer unitId = getUnitId();
		return createRange(from, to, unitId);
	}

	@Override
	protected void setValueInFields(T value) {
		N from = value.getFrom();
		N to = value.getTo();
		Integer unitId = value.getUnitId();
		getFromField().setValue(from);
		getToField().setValue(to);
		getUnitField().setValue(unitId);
	}

	@Override
	protected boolean calculateAllFieldsFilled() {
		return getFromField().hasValue() && getToField().hasValue();
	}

	@Override
	protected boolean calculateIsEmpty() {
		return getFromField().getValue() == null && getToField().getValue() == null;
	}

	protected abstract T createRange(N from, N to, Integer unitId);

}
