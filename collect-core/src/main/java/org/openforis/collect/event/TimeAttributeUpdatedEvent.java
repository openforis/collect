package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class TimeAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final Date time;
	
	public TimeAttributeUpdatedEvent(String surveyName, Integer recordId, String definitionId, 
			List<String> ancestorIds, String nodeId, Date time, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, ancestorIds, nodeId, timestamp, userName);
		this.time = time;
	}

	public Date getTime() {
		return time;
	}
}
