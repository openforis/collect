package org.openforis.collect.event;

import java.util.Date;

public class RootEntityCreatedEvent extends EntityCreatedEvent {

	public RootEntityCreatedEvent(Integer recordId, int definitionId,
			int rootEntityId, Date timestamp, String userName) {
		super(recordId, definitionId, null, rootEntityId, timestamp, userName);
	}

}
