package org.openforis.collect.event;

import java.util.Date;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class DateAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private Date date;

	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
}
