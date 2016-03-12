package org.openforis.idm.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author G. Miceli
 *
 */
public final class TextValue extends AbstractValue {

	public static final String VALUE_FIELD = "value";
	
	private String value;
	
	public TextValue(String value) {
		this.value = value;
	}
	
	@Override
	@SuppressWarnings("serial")
	public Map<String, Object> toMap() {
		return new HashMap<String, Object>() {{
			put(VALUE_FIELD, value);
		}};
	}
	
	public String getValue() {
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
		return value;
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
		TextValue other = (TextValue) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
