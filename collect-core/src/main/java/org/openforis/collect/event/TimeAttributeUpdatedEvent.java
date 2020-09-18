package org.openforis.collect.event;

import java.util.Date;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class TimeAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private Date time;

	public Date getTime() {
		return time;
	}
	
	public void setTime(Date time) {
		this.time = time;
	}
}
