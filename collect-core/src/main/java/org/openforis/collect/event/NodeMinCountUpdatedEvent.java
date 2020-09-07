package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class NodeMinCountUpdatedEvent extends NodeCountUpdatedEvent {

	public NodeMinCountUpdatedEvent(String surveyName, Integer recordId, RecordStep recordStep, String definitionId,
			List<String> ancestorIds, String nodeId, Date timestamp, String userName, int childDefinitionId,
			int minCount) {
		super(surveyName, recordId, recordStep, definitionId, ancestorIds, nodeId, timestamp, userName,
				childDefinitionId, minCount);
	}

}
