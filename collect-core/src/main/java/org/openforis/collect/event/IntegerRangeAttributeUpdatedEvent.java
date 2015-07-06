package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class IntegerRangeAttributeUpdatedEvent extends RangeAttributeUpdatedEvent<Integer> {

	public IntegerRangeAttributeUpdatedEvent(String surveyName, Integer recordId,
			String definitionId, List<String> ancestorIds, String nodeId, Integer from,
			Integer to, Integer unitId, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, ancestorIds, nodeId, from, to, Integer.class,
				unitId, timestamp, userName);
	}

}
