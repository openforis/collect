package org.openforis.collect.util;


/**
 * 
 * @author S. Ricci
 *
 */
public class NumberUtil {

	public static Double toDouble(Number value) {
		if ( value == null ) {
			return null;
		} else if ( value instanceof Float ) {
			//TODO to be improved
			//using doubleValue can lead to a wrong representation of the floating point number
			//but even to string cannot be used when the precision is too high...
			String strValue = value.toString();
			return new Double(strValue);
		} else {
			return value.doubleValue();
		}
	}
	
}
