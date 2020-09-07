package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class EntityUpdatedEvent extends EntityEvent {

	public EntityUpdatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				timestamp, userName);
	}
}
