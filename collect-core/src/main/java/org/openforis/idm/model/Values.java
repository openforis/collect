package org.openforis.idm.model;

public abstract class Values {

	public static String[] toStringValues(Value[] values) {
		String[] stringValues = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			Value value = values[i];
			stringValues[i] = value == null ? null : ((AbstractValue) value).toInternalString();
		}
		return stringValues;
	}
	
}
