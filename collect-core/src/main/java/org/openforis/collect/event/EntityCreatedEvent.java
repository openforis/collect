package org.openforis.collect.event;

import java.util.Date;

public class EntityCreatedEvent extends RecordEvent {

	public EntityCreatedEvent(Integer recordId, int definitionId,
			Integer parentId, int entityId, Date timestamp, String userName) {
		super(recordId, definitionId, parentId, entityId, timestamp, userName);
	}
	
}
