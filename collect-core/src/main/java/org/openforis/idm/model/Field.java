package org.openforis.idm.model;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.FieldDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class Field<T> extends Node<FieldDefinition<T>> implements Serializable, Comparable<Field<T>> {
	
	private static final long serialVersionUID = 1;
	
	/* WARNING: deleting or reordering fields will break protostuff deserialization! */
	
	Class<T> valueType;
	T value;
	String remarks;
	Character symbol;
	Attribute<?,?> attribute;
	State state;
	
	public Field(FieldDefinition<T> definition, Class<T> valueType, Attribute<?,?> attribute) {
		this.definition = definition;
		this.valueType = valueType;
		this.attribute = attribute;
		state = new State();
	}
	
	public Field(FieldDefinition<T> definition, Class<T> valueType) {
		this(definition, valueType, null);
	}
	
	@Override
	public int getIndex() {
		if ( attribute != null ) {
			List<Field<?>> siblings = attribute.getFields();
			int result = siblings.indexOf(this);
			return result;
		} else {
			return -1;
		}
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
		this.symbol = null;
	}
	
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Character getSymbol() {
		return symbol;
	}

	public void setSymbol(Character symbol) {
		this.symbol = symbol;
	}

	public boolean hasValue() {
		return value != null && ! value.toString().trim().isEmpty();
	}

	public boolean hasData() {
		return ! isEmpty();
	}

	public boolean isEmpty() {
		return ! hasValue() && StringUtils.isBlank(remarks) && symbol == null && (state == null || state.intValue() == 0);
	}
	
	public State getState() {
		return state;
	}
	
	public Attribute<?, ?> getAttribute() {
		return attribute;
	}
	
	@SuppressWarnings("unchecked")
	public T parseValue(String s) {
		if ( StringUtils.isBlank(s) ) {
			return null;
		} else if ( valueType == Boolean.class ) {
			return (T) Boolean.valueOf(s);
		} else if ( valueType == Integer.class ) {
			return (T) Integer.valueOf(s);
		} else if ( valueType == Long.class ) {
			return (T) Long.valueOf(s);
		} else if ( valueType == Double.class ) {
			return (T) Double.valueOf(s);
		} else if ( valueType == String.class ) {
			return (T) s;
		} else {
			throw new UnsupportedOperationException("AttributeField<"+valueType.getName()+"> not supported");
		}
	}

	public void setValueFromString(String s) {
		this.value = parseValue(s);
	}

	/**
	 * Reset all properties (value, remarks and symbol)
	 */
	public void clear() {
		this.value = null;
		this.remarks = null;
		this.symbol = null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public int compareTo(Field<T> o) {
		return ObjectUtils.compare((Comparable<Object>) value, (Comparable<Object>) o.value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((remarks == null) ? 0 : remarks.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result
				+ ((valueType == null) ? 0 : valueType.hashCode());
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
		Field<?> other = (Field<?>) obj;
		if (remarks == null) {
			if (other.remarks != null)
				return false;
		} else if (!remarks.equals(other.remarks))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (valueType == null) {
			if (other.valueType != null)
				return false;
		} else if (!valueType.equals(other.valueType))
			return false;
		return true;
	}

	@Override
	@Deprecated
	protected void write(StringWriter sw, int indent) {
		sw.append(String.valueOf(value));
	}
	
	public void setAttribute(Attribute<?, ?> attribute) {
		this.attribute = attribute;
	}	
}
