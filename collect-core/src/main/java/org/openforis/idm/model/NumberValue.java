package org.openforis.idm.model;

import java.util.HashMap;
import java.util.Map;

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
	private Integer unitId;
	
	public NumberValue(T value) {
		this(value, null);
	}
	
	public NumberValue(T value, Integer unitId) {
		this.value = value;
		this.unitId = unitId;
	}

	public T getValue() {
		return value;
	}
	
	public Integer getUnitId() {
		return unitId;
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
			put(UNIT_ID_FIELD, unitId);
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
		result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
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
		if (unitId == null) {
			if (other.unitId != null)
				return false;
		} else if (!unitId.equals(other.unitId))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
