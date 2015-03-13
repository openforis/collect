package org.openforis.idm.model;

import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class Time implements Value {
	/**
	 * Internal string format for Time value ("hhss")
	 */
	private static final Pattern INTERNAL_STRING_FORMAT = Pattern.compile("([01][0-9]|2[0-3])([0-5][0-9])");
	/**
	 * Generic string format for Time value ("hh:mm" or "hh:mm:ss" . Please note that seconds will be ignored by the Time attribute)
	 */
	private static final Pattern PRETTY_STRING_FORMAT = Pattern.compile("([0-9]|0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])(:[0-5][0-9])?");

	public static Time parseTime(String string) {
		if ( StringUtils.isBlank(string) ) {
			return null;
		} else {
			Matcher matcher = PRETTY_STRING_FORMAT.matcher(string);
			if ( ! matcher.matches() ) {
				matcher = INTERNAL_STRING_FORMAT.matcher(string);
				if ( ! matcher.matches() ) {
					throw new IllegalArgumentException("Invalid time " + string);
				}
			}
			int hour = Integer.parseInt(matcher.group(1));
			int minute = Integer.parseInt(matcher.group(2));
			return new Time(hour, minute);
		}
	}
	
	public static Time parse(java.util.Date date) {
		if ( date == null ) {
			return null;
		} else {
			Calendar cal = Calendar.getInstance();
		    cal.setTime(date);
		    int hour = cal.get(Calendar.HOUR);
		    if (cal.get(Calendar.AM_PM) == Calendar.PM) {
		    	hour += 12;
		    }
		    int minute = cal.get(Calendar.MINUTE);
		    return new Time(hour, minute);
		}
	}
	
	private final Integer hour;
	private final Integer minute;

	public Time(Integer hour, Integer minute) {
		this.hour = hour;
		this.minute = minute;
	}
	
	public Integer getHour() {
		return hour;
	}

	public Integer getMinute() {
		return minute;
	}
	
	public Calendar toCalendar() {
		if (hour == null || minute == null) {
			return null;
		} else {
			GregorianCalendar cal = new GregorianCalendar();
			cal.clear();
			cal.setLenient(false);
			boolean pm = hour > 12 || (hour == 12 && minute > 0);
			int calHour = pm ? hour - 12: hour;
			cal.set(Calendar.AM_PM, pm ? Calendar.PM: Calendar.AM);
			cal.set(Calendar.HOUR, calHour);
			cal.set(Calendar.MINUTE, minute);
			return cal;
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("hour", hour)
			.append("minute", minute)
			.toString();
	}

	public String toXmlTime() {
		if ( hour == null || minute == null ) {
			return null;
		} else {
			Formatter formatter = new Formatter();
			formatter.format("%02d:%02d:00", hour, minute);
			String result = formatter.toString();
			formatter.close();
			return result;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hour == null) ? 0 : hour.hashCode());
		result = prime * result + ((minute == null) ? 0 : minute.hashCode());
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
		Time other = (Time) obj;
		if (hour == null) {
			if (other.hour != null)
				return false;
		} else if (!hour.equals(other.hour))
			return false;
		if (minute == null) {
			if (other.minute != null)
				return false;
		} else if (!minute.equals(other.minute))
			return false;
		return true;
	}

}
