/**
 * 
 */
package org.openforis.collect.model;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openforis.idm.model.TimestampValue;

/**
 * @author M. Togna
 * 
 */
public abstract class AbstractTimestampValue extends AbstractValue implements TimestampValue {

	public AbstractTimestampValue(String stringValue) {
		super(stringValue);
	}

	/**
	 * Returns a calendar with neither date nor time specified
	 */
	protected Calendar getDefaultCalendar() {
		GregorianCalendar calendar = new GregorianCalendar(0, 0, 0, 0, 0, 0);
		return calendar;
	}

}
