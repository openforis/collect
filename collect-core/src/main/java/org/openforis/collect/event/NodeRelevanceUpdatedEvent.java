package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class NodeRelevanceUpdatedEvent extends RecordEvent  {

	private int childDefinitionId;
	private boolean relevant;

	public NodeRelevanceUpdatedEvent(String surveyName, Integer recordId, RecordStep recordStep, String definitionId,
			List<String> ancestorIds, String nodeId, Date timestamp, String userName, int childDefinitionId,
			boolean relevant) {
		super(surveyName, recordId, recordStep, definitionId, ancestorIds, nodeId, timestamp, userName);
		this.childDefinitionId = childDefinitionId;
		this.relevant = relevant;
	}
	
	public int getChildDefinitionId() {
		return childDefinitionId;
	}
	
	public boolean isRelevant() {
		return relevant;
	}

}
