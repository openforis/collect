package org.openforis.collect.event;

import java.util.Date;

public abstract class AttributeUpdatedEvent extends RecordEvent {

	public AttributeUpdatedEvent(Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Date timestamp, String userName) {
		super(recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
	}

}
