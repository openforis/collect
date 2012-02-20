/**
 * 
 */
package org.openforis.collect.model;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.openforis.idm.model.TimeValue;

/**
 * @author M. Togna
 * 
 */
public class TimeValueImpl extends AbstractTimestampValue implements TimeValue {

	private static final String TIME_SEPARATOR = ":";
	private static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3])" + TIME_SEPARATOR + "[0-5][0-9]";
	private static final Pattern PATTERN = Pattern.compile(TIME24HOURS_PATTERN);;

	public TimeValueImpl(String stringValue) {
		super(stringValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.TimestampValue#toCalendar()
	 */
	@Override
	public Calendar toCalendar() {
		if (this.isValidTime()) {
			Calendar calendar = this.getDefaultCalendar();
			String[] strings = this.getText1().split(TIME_SEPARATOR);
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strings[0]));
			calendar.set(Calendar.MINUTE, Integer.parseInt(strings[1]));
			return calendar;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.TimeValue#getHour()
	 */
	@Override
	public Integer getHour() {
		if (this.isValidTime()) {
			String[] strings = this.getText1().split(TIME_SEPARATOR);
			return Integer.parseInt(strings[0]);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.TimeValue#getMinute()
	 */
	@Override
	public Integer getMinute() {
		if (this.isValidTime()) {
			String[] strings = this.getText1().split(TIME_SEPARATOR);
			return Integer.parseInt(strings[1]);
		}
		return null;
	}

	private boolean isValidTime() {
		if (!this.isBlank()) {
			Matcher matcher = PATTERN.matcher(this.getText1());
			return matcher.matches();
		}
		return false;
	}

	@Override
	public boolean isFormatValid() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeValueImpl other = (TimeValueImpl) obj;
		return new EqualsBuilder().append(this.getHour(), other.getHour()).append(this.getMinute(), other.getMinute()).isEquals();
	}

}
