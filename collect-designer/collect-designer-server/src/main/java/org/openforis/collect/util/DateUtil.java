package org.openforis.collect.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

/**
 * 
 * @author S. Ricci
 *
 */
public class DateUtil {

	private static final String XML_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	public static Date parseXMLDateTime(String dateTime) {
		if ( dateTime != null ) {
			Calendar cal = DatatypeConverter.parseDateTime(dateTime);
			Date result = cal.getTime();
			return result;
		} else {
			return null;
		}
	}
	
	public static String formatStringDate(String dateTimeStr, String format) {
		Date dateTime = parseXMLDateTime(dateTimeStr);
		String result = formatDate(dateTime, format);
		return result;
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
		return formatDate(dateTime, XML_DATE_TIME_FORMAT);
	}
	
}
