/**
 * 
 */
package org.openforis.collect.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.openforis.idm.model.DateValue;

/**
 * @author M. Togna
 * 
 */
public class DateValueImpl extends AbstractTimestampValue implements DateValue {

	protected static String DATE_PATTERN = "yyyy-MM-dd";
	protected static DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

	public DateValueImpl(String stringValue) {
		super(stringValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.TimestampValue#toCalendar()
	 */
	@Override
	public Calendar toCalendar() {
		Date date = this.getDate();
		if (date != null) {
			Calendar calendar = this.getDefaultCalendar();
			calendar.setTime(date);
			return calendar;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.DateValue#getDay()
	 */
	@Override
	public Integer getDay() {
		Calendar calendar = this.toCalendar();
		if (calendar != null) {
			return calendar.get(Calendar.DAY_OF_MONTH);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.DateValue#getMonth()
	 */
	@Override
	public Integer getMonth() {
		Calendar calendar = this.toCalendar();
		if (calendar != null) {
			return calendar.get(Calendar.MONTH) + 1;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.DateValue#getYear()
	 */
	@Override
	public Integer getYear() {
		Calendar calendar = this.toCalendar();
		if (calendar != null) {
			return calendar.get(Calendar.YEAR);
		}
		return null;
	}

	/**
	 * @throws ParseException
	 */
	private Date getDate() {
		Date date = null;
		try {
			date = DATE_FORMAT.parse(this.getText1());
		} catch (ParseException e) {
			// invalid date
		}
		return date;

	}

	@Override
	public boolean isFormatValid() {
		return getDate() != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DateValueImpl other = (DateValueImpl) obj;
		return other.getDate().equals(this.getDate());
	}

}
