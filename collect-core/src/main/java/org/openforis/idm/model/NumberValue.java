package org.openforis.idm.model;

import org.openforis.idm.metamodel.Unit;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class NumberValue<T extends Number> implements Value {

	private T value;
	private Unit unit;
	
	public NumberValue(T value, Unit unit) {
		this.value = value;
		this.unit = unit;
	}

	public T getValue() {
		return value;
	}
	
	public Unit getUnit() {
		return unit;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NumberValue<?> other = (NumberValue<?>) obj;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("value: %s ; unit: %s", value, unit);
	}
}
