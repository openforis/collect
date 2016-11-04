package org.openforis.collect.utils;

public abstract class Numbers {
	
	public static int toInt(Integer value) {
		return toInt(value, 0);
	}

	public static int toInt(Integer value, int defaultValue) {
		return value == null ? defaultValue : value.intValue();
	}

}
