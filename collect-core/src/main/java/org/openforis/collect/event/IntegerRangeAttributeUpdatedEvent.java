package org.openforis.collect.event;

import java.util.Date;

public class IntegerRangeAttributeUpdatedEvent extends RangeAttributeUpdatedEvent<Integer> {

	public IntegerRangeAttributeUpdatedEvent(String surveyName, Integer recordId,
			int definitionId, Integer parentEntityId, int nodeId, Integer from,
			Integer to, Integer unitId, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, parentEntityId, nodeId, from, to, Integer.class,
				unitId, timestamp, userName);
	}

}
