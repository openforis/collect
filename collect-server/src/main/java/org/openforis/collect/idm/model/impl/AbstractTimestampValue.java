/**
 * 
 */
package org.openforis.collect.idm.model.impl;

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
	 * 
	 */
	protected Calendar getDefaultCalendar() {
		GregorianCalendar calendar = new GregorianCalendar(0, 0, 0, 0, 0, 0);
		return calendar;
	}

}
