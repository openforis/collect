package org.openforis.collect.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class Dates {

	private static final String XML_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String XML_DATE_FORMAT = "yyyy-MM-dd";

	public static Date parseXMLDateTime(String dateTime) {
		if ( StringUtils.isNotBlank(dateTime) ) {
			Calendar cal = DatatypeConverter.parseDateTime(dateTime);
			Date result = cal.getTime();
			return result;
		} else {
			return null;
		}
	}
	
	public static Date parseXMLDate(String date) {
		if ( StringUtils.isNotBlank(date) ) {
			Calendar cal = DatatypeConverter.parseDate(date);
			Date result = cal.getTime();
			return result;
		} else {
			return null;
		}
	}
	
	public static String formatStringDate(String dateTimeStr, String format) {
		String result = null;
		if ( StringUtils.isNotBlank(dateTimeStr) ) {
			Date dateTime = parseXMLDateTime(dateTimeStr);
			result = formatDate(dateTime, format);
		}
		return result;
	}

	public static String formatStringDateInXMLFormat(String dateTimeStr) {
		return formatStringDate(dateTimeStr, XML_DATE_TIME_FORMAT);
	}
	
	public static String formatDate(Date dateTime, String format) {
		if ( dateTime != null ) {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			String result = formatter.format(dateTime);
			return result;
		} else {
			return null;
		}
	}
	
	public static String formatDateToXML(Date dateTime) {
		return formatDate(dateTime, XML_DATE_FORMAT);
	}
	
	public static String formatDateTimeToXML(Date dateTime) {
		return formatDate(dateTime, XML_DATE_TIME_FORMAT);
	}

}
