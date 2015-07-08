package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class EntityCreatedEvent extends RecordEvent {

	public EntityCreatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				timestamp, userName);
	}

}
