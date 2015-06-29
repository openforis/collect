package org.openforis.collect.event;

import java.util.Date;

public class BooleanAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final Boolean value;
	
	public BooleanAttributeUpdatedEvent(String surveyName, Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Boolean value, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
		this.value = value;
	}

	public Boolean getValue() {
		return value;
	}
	
}
