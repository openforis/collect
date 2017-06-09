package org.openforis.idm.model;

import java.util.HashMap;
import java.util.Map;

import org.openforis.idm.metamodel.Unit;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public abstract class NumberValue<T extends Number> extends AbstractValue {

	public static final String VALUE_FIELD = "value";
	public static final String UNIT_ID_FIELD = "unit_id";
	
	private T value;
	private Unit unit;
	
	public NumberValue(T value) {
		this(value, null);
	}
	
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
	public String toPrettyFormatString() {
		return toInternalString();
	}

	@Override
	public String toInternalString() {
		return getValue() == null ? null: getValue().toString();
	}
	
	@Override
	@SuppressWarnings("serial")
	public Map<String, Object> toMap() {
		return new HashMap<String, Object>() {{
			put(VALUE_FIELD, value);
			put(UNIT_ID_FIELD, unit == null ? null: unit.getId());
		}};
	}
	
	@Override
	public String toString() {
		return toPrettyFormatString();
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

}
