package org.openforis.collect.event;

import java.util.Date;

public class DateAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final Date date;
	
	public DateAttributeUpdatedEvent(String surveyName, Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Date date, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
		this.date = date;
	}

	public Date getDate() {
		return date;
	}
}
