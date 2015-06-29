package org.openforis.collect.event;

import java.util.Date;

public abstract class AttributeUpdatedEvent extends RecordEvent {

	public AttributeUpdatedEvent(String surveyName, Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
	}

}
