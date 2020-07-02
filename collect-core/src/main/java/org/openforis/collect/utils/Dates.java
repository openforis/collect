package org.openforis.collect.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class Dates {

	private static final String LOCAL_DATE_TIME_FORMAT 	= "yyyy-MM-dd'T'HH:mm:ss";
	private static final String DATE_TIME_FORMAT 	= "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private static final String COMPACT_DATE_TIME_FORMAT 	= "yyyyMMdd'T'HHmmss";
	private static final String DATE_FORMAT 		= "yyyy-MM-dd";

	public static Date millisToDate(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		return cal.getTime();
	}
	
	public static Date parseDateTime(String dateTime) {
		if ( StringUtils.isBlank(dateTime) ) {
			return null;
		}
		dateTime = dateTime.trim();
		int length = dateTime.length();
		if ( length == DATE_FORMAT.length() ) {
			return parse(dateTime, DATE_FORMAT);
		} else if (length > LOCAL_DATE_TIME_FORMAT.length()) {
			return parseDateTimeInternal(dateTime);
		} else {
			return parse(dateTime, LOCAL_DATE_TIME_FORMAT);
		}
	}

	private static Date parseDateTimeInternal(String dateTime) {
		int lastIndexOfC = dateTime.lastIndexOf(":");
		dateTime = dateTime.substring(0, lastIndexOfC) + dateTime.substring(lastIndexOfC + 1, dateTime.length());
		return parse(dateTime, DATE_TIME_FORMAT);
	}
	
	public static Date parseDate(String date) {
		if ( StringUtils.isBlank(date) ) {
			return null;
		}
		date = date.trim();
		if ( date.length() > DATE_FORMAT.length() ) {
			date = date.substring(0, DATE_FORMAT.length());
		}
		return parse(date, DATE_FORMAT);
	}

	public static Date parse(String dateString, String format) {
		if ( dateString == null ) {
			return null;
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			try {
				return formatter.parse(dateString);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static String formatDate(Date dateTime) {
		return format(dateTime, DATE_FORMAT);
	}
	
	public static String formatLocalDateTime(Date dateTime) {
		return format(dateTime, LOCAL_DATE_TIME_FORMAT);
	}
	
	public static Object formatCompactDateTime(Date dateTime) {
		return format(dateTime, COMPACT_DATE_TIME_FORMAT);
	}
	
	public static Object formatCompactNow() {
		return formatCompactDateTime(new Date());
	}

	public static String formatDateTime(Date dateTime) {
		String value = format(dateTime, DATE_TIME_FORMAT);
		if (value == null) {
			return null;
		}
		value = value.substring(0, value.length() - 2) + ":" + value.substring(value.length() - 2, value.length());
		return value;
	}

	public static String format(Date dateTime, String format) {
		if ( dateTime == null ) {
			return null;
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			String result = formatter.format(dateTime);
			return result;
		}
	}

	public static int compareDateOnly(Date date1, Date date2) {
		Date onlyDate1 = toOnlyDate(date1);
		Date onlyDate2 = toOnlyDate(date2);
		return onlyDate1.compareTo(onlyDate2);
	}

	public static int compareUpToMinutesOnly(Date date1, Date date2) {
		Date dt1 = toOnlyDateAndTimeToMinutes(date1);
		Date dt2 = toOnlyDateAndTimeToMinutes(date2);
		return dt1.compareTo(dt2);
	}

	public static int compareUpToSecondsOnly(Date date1, Date date2) {
		Date dt1 = toOnlyDateAndTimeToSeconds(date1);
		Date dt2 = toOnlyDateAndTimeToSeconds(date2);
		return dt1.compareTo(dt2);
	}

	public static Date toOnlyDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Date toOnlyDateAndTimeToMinutes(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Date toOnlyDateAndTimeToSeconds(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

}
