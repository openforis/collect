package org.openforis.collect.event;

import java.util.Date;

public class BooleanAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final Boolean value;
	
	public BooleanAttributeUpdatedEvent(Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Boolean value, Date timestamp, String userName) {
		super(recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
		this.value = value;
	}

	public Boolean getValue() {
		return value;
	}
	
}
