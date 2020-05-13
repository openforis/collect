package org.openforis.collect.utils;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class Numbers {

	public static int toInt(Integer value) {
		return toInt(value, 0);
	}

	public static int toInt(Integer value, int defaultValue) {
		return value == null ? defaultValue : value.intValue();
	}

	public static int sum(Integer... values) {
		int total = 0;
		for (Integer value : values) {
			if (value != null) {
				total += value;
			}
		}
		return total;
	}

	public static double sum(Double... values) {
		double total = 0;
		for (Double value : values) {
			if (value != null) {
				total += value;
			}
		}
		return total;
	}

	public static boolean isNumber(Object value) {
		return value != null
				&& (value instanceof Number || (value instanceof String && NumberUtils.isCreatable((String) value)));
	}
	
	public static Double toDouble(Object value) {
		if (value instanceof String) {
			return Double.valueOf((String) value);
		} else if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			return null;
		}
	}
}
