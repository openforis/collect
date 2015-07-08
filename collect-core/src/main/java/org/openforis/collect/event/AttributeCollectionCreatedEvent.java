package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class AttributeCollectionCreatedEvent extends RecordEvent {

	public AttributeCollectionCreatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				timestamp, userName);
	}

}
