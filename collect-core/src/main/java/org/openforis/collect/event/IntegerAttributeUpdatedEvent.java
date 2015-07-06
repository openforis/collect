package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class IntegerAttributeUpdatedEvent extends NumberAttributeUpdatedEvent<Integer> {

	public IntegerAttributeUpdatedEvent(String surveyName, Integer recordId, String definitionId, 
			List<String> ancestorIds, String nodeId, Integer value, Integer unitId, Date timestamp,
			String userName) {
		super(surveyName, recordId, definitionId, ancestorIds, nodeId, value, Integer.class, unitId,
				timestamp, userName);
	}

}
