package org.openforis.idm.model;

import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public final class Date extends AbstractValue {

	private static final Pattern INTERNAL_PATTERN = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})");
	private static final Pattern PRETTY_INTERNAL_PATTERN = Pattern.compile("(\\d{4})\\-(\\d{2})\\-(\\d{2})");
	private static final Pattern PRETTY_PATTERN = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})");
	private static final Pattern[] INTERNAL_FORMAT_PATTERNS = new Pattern[]{INTERNAL_PATTERN, PRETTY_INTERNAL_PATTERN};
	
	private static final String PRETTY_FORMAT = "%02d/%02d/%04d";
	private static final String INTERNAL_FORMAT = "%04d-%02d-%02d";
	
	public static final String YEAR_FIELD = "year";
	public static final String MONTH_FIELD = "month";
	public static final String DAY_FIELD = "day";
	
	private final Integer day;
	private final Integer month;
	private final Integer year;

	public Date(Integer year, Integer month, Integer day) {
		this.year = year;
		this.month = month;
		this.day = day;		
	}

	public static Date parse(String value){
		if ( StringUtils.isBlank(value) ) {
			return null;
		}
		if (matches(value, INTERNAL_FORMAT_PATTERNS)) {
			return parseInInternalFormat(value);
		} else if (matches(value, PRETTY_PATTERN)) {
			return parseInPrettyFormat(value);
		} else {
			throw new IllegalArgumentException("Invalid date format: " + value);
		}
	}
	
	private static boolean matches(String value, Pattern...patterns) {
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(value);
			if (matcher.matches()) {
				return true;
			}
		}
		return false;
	}

	private static Date parseInInternalFormat(String value) {
		Matcher matcher = findMatcher(value, INTERNAL_FORMAT_PATTERNS);
		int year = Integer.parseInt(matcher.group(1));
		int month = Integer.parseInt(matcher.group(2));
		int day = Integer.parseInt(matcher.group(3));
		return new Date(year, month, day);
	}

	private static Date parseInPrettyFormat(String value) {
		Matcher matcher = findMatcher(value, PRETTY_PATTERN);
		int day = Integer.parseInt(matcher.group(1));
		int month = Integer.parseInt(matcher.group(2));
		int year = Integer.parseInt(matcher.group(3));
		return new Date(year, month, day);
	}
	
	private static Matcher findMatcher(String string, Pattern... patterns) {
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(string);
			if (matcher.matches()) {
				return matcher;
			}
		}
		throw new IllegalArgumentException("Invalid date " + string);
	}

	public static Date parse(java.util.Date date) {
		if ( date == null ) {
			return null;
		} else {
			Calendar cal = Calendar.getInstance();
		    cal.setTime(date);
		    int year = cal.get(Calendar.YEAR);
		    int month = cal.get(Calendar.MONTH);
		    int day = cal.get(Calendar.DAY_OF_MONTH);
		    return new Date(year, month + 1, day);
		}
	}
	
	@Override
	@SuppressWarnings("serial")
	public Map<String, Object> toMap() {
		return new HashMap<String, Object>() {{
			put(YEAR_FIELD, year);
			put(MONTH_FIELD, month);
			put(DAY_FIELD, day);
		}};
	}
	
	@Override
	public String toPrettyFormatString() {
		return String.format(PRETTY_FORMAT, day, month, year);
	}
	
	@Override
	public String toInternalString() {
		return toXmlDate();
	}
	
	public static Date fromNumericValue(Integer value) {
		if (value == null) {
			return null;
		}
		//20150520
		int yearPart = Double.valueOf(Math.floor((double) (value / 10000))).intValue();
		int monthPart = Double.valueOf(Math.floor((double) ((value % 10000) / 100))).intValue();
		int dayPart = value % 100;
		return new Date(yearPart, monthPart, dayPart);
	}
	
	public Integer getDay() {
		return day;
	}
	
	public Integer getMonth() {
		return month;
	}
	
	public Integer getYear() {
		return year;
	}
	
	public boolean isComplete() {
		return year != null && month != null && day != null;
	}
	
	public Calendar toCalendar() {
		if (isComplete()) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.clear();
			cal.setLenient(false);
			cal.set(year, month-1, day);
			return cal;
		} else {
			return null;
		}
	}
	
	public java.util.Date toJavaDate() {
		try {
			Calendar cal = toCalendar();
			return cal == null ? null: cal.getTime();
		} catch(Exception e) {
			//invalid date, ignore it
			return null;
		}
	}

	public String toXmlDate() {
		Formatter formatter = new Formatter();
		formatter.format(INTERNAL_FORMAT, year, month, day);
		String result = formatter.toString();
		formatter.close();
		return result;
	}
	

	public Integer getNumericValue() {
		if (isComplete()) {
			return (year * 10000) + (month * 100) + day;
		} else {
			return null;
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + ((month == null) ? 0 : month.hashCode());
		result = prime * result + ((year == null) ? 0 : year.hashCode());
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
		Date other = (Date) obj;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.equals(other.day))
			return false;
		if (month == null) {
			if (other.month != null)
				return false;
		} else if (!month.equals(other.month))
			return false;
		if (year == null) {
			if (other.year != null)
				return false;
		} else if (!year.equals(other.year))
			return false;
		return true;
	}
	
}
