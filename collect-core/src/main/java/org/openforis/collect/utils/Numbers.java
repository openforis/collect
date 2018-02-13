package org.openforis.collect.utils;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class Numbers {

	public static int addAll(Integer... values) {
		int total = 0;
		for (Integer value : values) {
			if (value != null) {
				total += value;
			}
		}
		return total;
	}
	
	public static double addAll(Double... values) {
		double total = 0;
		for (Double value : values) {
			if (value != null) {
				total += value;
			}
		}
		return total;
	}
	
}
