package org.openforis.collect.event;

import java.util.Date;

public class IntegerAttributeUpdatedEvent extends NumericAttributeUpdatedEvent<Integer> {

	public IntegerAttributeUpdatedEvent(Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Integer value, Integer unitId, Date timestamp,
			String userName) {
		super(recordId, definitionId, parentEntityId, nodeId, value, Integer.class, unitId,
				timestamp, userName);
	}

}
