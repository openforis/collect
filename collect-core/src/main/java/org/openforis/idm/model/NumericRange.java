package org.openforis.idm.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class NumericRange<T extends Number> extends AbstractValue {

	public static final String FROM_FIELD = "from";
	public static final String TO_FIELD = "to";
	public static final String UNIT_ID_FIELD = "unit_id";
	
	private final T from;
	private final T to;
	private final Integer unitId;
	
	protected static final String DELIM = "-";

	NumericRange(T from, T to, Integer unitId) {
		this.from = from;
		this.to = to;
		this.unitId = unitId;
	}

	public T getFrom() {
		return from;
	}

	public T getTo() {
		return to;
	}

	public Integer getUnitId() {
		return unitId;
	}
	
	@Override
	@SuppressWarnings("serial")
	public Map<String, Object> toMap() {
		return new HashMap<String, Object>() {{
			put(FROM_FIELD, from);
			put(TO_FIELD, to);
			put(UNIT_ID_FIELD, unitId);
		}};
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		@SuppressWarnings("unchecked")
		NumericRange<T> other = (NumericRange<T>) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

}
