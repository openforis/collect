package org.openforis.collect.event;

import java.util.Date;

public class DoubleRangeAttributeUpdatedEvent extends RangeAttributeUpdatedEvent<Double> {

	public DoubleRangeAttributeUpdatedEvent(String surveyName, Integer recordId,
			int definitionId, Integer parentEntityId, int nodeId, Double from,
			Double to, Integer unitId,
			Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, parentEntityId, nodeId, from, to, Double.class,
				unitId, timestamp, userName);
	}

}
