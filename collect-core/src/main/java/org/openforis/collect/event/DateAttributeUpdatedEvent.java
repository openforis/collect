package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class DateAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final Date date;
	
	public DateAttributeUpdatedEvent(String surveyName, Integer recordId, String definitionId, List<String> ancestorIds, 
			String nodeId, Date date, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, ancestorIds, nodeId, timestamp, userName);
		this.date = date;
	}

	public Date getDate() {
		return date;
	}
}
