package org.openforis.idm.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author G. Miceli
 *
 */
public final class BooleanValue extends AbstractValue {

	public static final String VALUE_FIELD = "value";
	
	private Boolean value;
	
	public BooleanValue(List<String> fieldValues) {
		this(fieldValues == null || fieldValues.isEmpty() ? null : fieldValues.get(0));
	}
	
	public BooleanValue(Boolean value) {
		this.value = value;
	}
	
	public BooleanValue(String string) {
		if ( StringUtils.isBlank(string) ) {
			this.value = null;
		} else {
			this.value = Boolean.parseBoolean(string);
		}
	}
	
	public int compareTo(Value o) {
		if ( o instanceof BooleanValue ) {
			return ObjectUtils.compare(value, ((BooleanValue) o).value);
		} else {
			throw new IllegalArgumentException("Cannot compare boolean value with " + o.getClass());
		}
	}
	
	@Override
	@SuppressWarnings("serial")
	public Map<String, Object> toMap() {
		return new HashMap<String, Object>() {{
			put(VALUE_FIELD, value);
		}};
	}
	
	public Boolean getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return toPrettyFormatString();
	}

	@Override
	public String toPrettyFormatString() {
		return toInternalString();
	}
	
	@Override
	public String toInternalString() {
		return value == null ? null: value.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		BooleanValue other = (BooleanValue) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
