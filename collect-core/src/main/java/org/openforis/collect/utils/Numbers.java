package org.openforis.collect.utils;

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

}
