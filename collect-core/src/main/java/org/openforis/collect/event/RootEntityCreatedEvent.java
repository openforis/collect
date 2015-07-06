package org.openforis.collect.event;

import java.util.Collections;
import java.util.Date;

public class RootEntityCreatedEvent extends EntityCreatedEvent {

	public RootEntityCreatedEvent(String surveyName, Integer recordId, String definitionId,
			String rootEntityId, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, Collections.<String>emptyList(), rootEntityId, timestamp, userName);
	}

}
