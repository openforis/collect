package org.openforis.collect.event;

import java.util.Date;

public class RootEntityCreatedEvent extends EntityCreatedEvent {

	public RootEntityCreatedEvent(String surveyName, Integer recordId, int definitionId,
			int rootEntityId, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, null, rootEntityId, timestamp, userName);
	}

}
