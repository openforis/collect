package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public abstract class NodeCountUpdatedEvent extends RecordEvent {

	private int childDefinitionId;
	private int count;

	public NodeCountUpdatedEvent(String surveyName, Integer recordId, RecordStep recordStep, String definitionId,
			List<String> ancestorIds, String nodeId, Date timestamp, String userName, int childDefinitionId,
			int count) {
		super(surveyName, recordId, recordStep, definitionId, ancestorIds, nodeId, timestamp, userName);
		this.childDefinitionId = childDefinitionId;
		this.count = count;
	}

	public int getChildDefinitionId() {
		return childDefinitionId;
	}

	public int getCount() {
		return count;
	}
}
