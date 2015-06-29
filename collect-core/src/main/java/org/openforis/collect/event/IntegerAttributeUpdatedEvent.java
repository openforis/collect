package org.openforis.collect.event;

import java.util.Date;

public class IntegerAttributeUpdatedEvent extends NumberAttributeUpdatedEvent<Integer> {

	public IntegerAttributeUpdatedEvent(String surveyName, Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Integer value, Integer unitId, Date timestamp,
			String userName) {
		super(surveyName, recordId, definitionId, parentEntityId, nodeId, value, Integer.class, unitId,
				timestamp, userName);
	}

}
