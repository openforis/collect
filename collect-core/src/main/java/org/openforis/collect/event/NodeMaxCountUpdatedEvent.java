package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class NodeMaxCountUpdatedEvent extends NodeCountUpdatedEvent  {

	public NodeMaxCountUpdatedEvent(String surveyName, Integer recordId, RecordStep recordStep, String definitionId,
			List<String> ancestorIds, String nodeId, Date timestamp, String userName, int childDefinitionId,
			int maxCount) {
		super(surveyName, recordId, recordStep, definitionId, ancestorIds, nodeId, timestamp, userName, childDefinitionId, maxCount);
	}
	
}
