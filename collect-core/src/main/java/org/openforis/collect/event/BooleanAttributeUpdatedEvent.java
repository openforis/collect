package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class BooleanAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final boolean value;
	
	public BooleanAttributeUpdatedEvent(String surveyName, Integer recordId,
			String definitionId, List<String> ancestorIds, String nodeId,
			boolean value, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, ancestorIds, nodeId, timestamp,
				userName);
		this.value = value;
	}

	public boolean getValue() {
		return value;
	}
	
}
