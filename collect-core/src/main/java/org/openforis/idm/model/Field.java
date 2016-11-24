package org.openforis.idm.model;

import java.io.Serializable;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.FieldDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class Field<T> extends Node<FieldDefinition<T>> implements Serializable, Comparable<Field<T>> {
	
	private static final long serialVersionUID = 1;
	
	private static final Double ZERO = Double.valueOf(0d);
	private static final Double NEGATIVE_ZERO = Double.valueOf(-0d);

	/* WARNING: deleting or reordering fields will break protostuff deserialization! */
	
	Class<T> valueType;
	T value;
	String remarks;
	Character symbol;
	Attribute<?,?> attribute;
	State state;
	
	public Field(FieldDefinition<T> definition, Class<T> valueType, Attribute<?,?> attribute) {
		super(definition);
		this.valueType = valueType;
		this.attribute = attribute;
		this.state = new State();
	}
	
	public Field(FieldDefinition<T> definition, Class<T> valueType) {
		this(definition, valueType, null);
	}
	
	public T getValue() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public void setValue(T value) {
		//replace -0 with 0 to avoid issues with XPath comparison 
		if (value instanceof Double && ((Double) value).equals(NEGATIVE_ZERO)) {
			value = (T) ZERO;
		}
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
		if (value == null) {
			return false;
		} else if (Number.class.isAssignableFrom(valueType)) {
			return true;
		} else {
			return ! value.toString().trim().isEmpty();
		}
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
	
	public void setAttribute(Attribute<?, ?> attribute) {
		this.attribute = attribute;
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
		try {
			T parsedValue = parseValue(s);
			setValue(parsedValue);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Class<T> getValueType() {
		return valueType;
	}

	/**
	 * Reset all properties (value, remarks and symbol)
	 */
	public void clear() {
		this.value = null;
		this.remarks = null;
		this.symbol = null;
	}
	
	public String getStringValue() {
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		} else if (value instanceof Number) {
			NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);
			return formatter.format(value);
		} else {
			return value.toString();
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public int compareTo(Field<T> o) {
		return ObjectUtils.compare((Comparable<Object>) value, (Comparable<Object>) o.value);
	}

	@Override
	@Deprecated
	protected void write(StringWriter sw, int indent) {
		sw.append(String.valueOf(value));
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

}
