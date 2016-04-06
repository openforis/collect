package org.openforis.idm.model;

import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class Time extends AbstractValue {

	/**
	 * Internal string format for Time value ("hhss")
	 */
	private static final Pattern INTERNAL_STRING_PATTERN = Pattern.compile("(\\d*)([0-5][0-9])");
	/**
	 * Generic string format for Time value ("hh:mm" or "hh:mm:ss" . Please note that seconds will be ignored by the Time attribute)
	 */
	private static final String PRETTY_FORMAT = "%02d:%02d";
//	private static final Pattern PRETTY_STRING_FORMAT = Pattern.compile("([0-9]|0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])(:[0-5][0-9])?");
	private static final Pattern PRETTY_STRING_FORMAT = Pattern.compile("(\\d+):(\\d{1,2})(:\\d{1,2})?");
	
	private static final Pattern[] PATTERNS = new Pattern[] {INTERNAL_STRING_PATTERN, PRETTY_STRING_FORMAT};
	
	public static Time parseTime(String value) {
		if ( StringUtils.isBlank(value) ) {
			return null;
		} else {
			for (Pattern pattern : PATTERNS) {
				Matcher matcher = pattern.matcher(value);
				if (matcher.matches()) {
					int hour = toInt(matcher.group(1));
					int minute = toInt(matcher.group(2));
					return new Time(hour, minute);
				}
			}
			throw new IllegalArgumentException("Invalid time " + value);
		}
	}
	
	private static int toInt(String str) {
		if (StringUtils.isBlank(str)) {
			return 0;
		} else {
			return Integer.parseInt(str);
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
	
	public static Time fromNumericValue(Integer value) {
		if (value == null) {
			return null;
		}
		int hourPart = Double.valueOf(Math.floor((double) (value / 100))).intValue();
		int minutePart = value % 100;
		return new Time(hourPart, minutePart);
	}
	
	private final Integer hour;
	private final Integer minute;

	public Time(Integer hour, Integer minute) {
		this.hour = hour;
		this.minute = minute;
	}
	
	public boolean isComplete() {
		return hour != null && minute != null;
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
			cal.setLenient(true);
			boolean pm = hour > 12 || (hour == 12 && minute > 0);
			int calHour = pm ? hour - 12: hour;
			cal.set(Calendar.AM_PM, pm ? Calendar.PM: Calendar.AM);
			cal.set(Calendar.HOUR, calHour);
			cal.set(Calendar.MINUTE, minute);
			return cal;
		}
	}
	
	public java.util.Date toJavaDate() {
		Calendar cal = toCalendar();
		return cal == null ? null: cal.getTime();
	}

	@Override
	@SuppressWarnings("serial")
	public Map<String, Object> toMap() {
		return new HashMap<String, Object>() {{
			put(TimeAttributeDefinition.HOUR_FIELD, hour);
			put(TimeAttributeDefinition.MINUTE_FIELD, minute);
		}};
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("hour", hour)
			.append("minute", minute)
			.toString();
	}

	public String toXmlTime() {
		if (isComplete()) {
			Formatter formatter = new Formatter();
			formatter.format("%02d:%02d:00", hour, minute);
			String result = formatter.toString();
			formatter.close();
			return result;
		} else {
			return null;
		}
	}
	
	public Integer getNumericValue() {
		if (isComplete()) {
			return hour * 100 + minute;
		} else {
			return null;
		}
	}

	@Override
	public String toPrettyFormatString() {
		return String.format(PRETTY_FORMAT, hour, minute);
	}
	
	@Override
	public String toInternalString() {
		return toXmlTime();
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
