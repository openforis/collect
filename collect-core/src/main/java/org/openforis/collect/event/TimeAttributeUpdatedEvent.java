package org.openforis.collect.event;

import java.util.Date;

public class TimeAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final Date time;
	
	public TimeAttributeUpdatedEvent(Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Date time, Date timestamp, String userName) {
		super(recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
		this.time = time;
	}

	public Date getTime() {
		return time;
	}
}
