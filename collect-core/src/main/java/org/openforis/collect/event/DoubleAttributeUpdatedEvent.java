package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class DoubleAttributeUpdatedEvent extends NumberAttributeUpdatedEvent<Double> {

	public DoubleAttributeUpdatedEvent(String surveyName, Integer recordId,
			String definitionId, List<String> ancestorIds, String nodeId,
			Double value, Integer unitId, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, ancestorIds, nodeId,
				value, Double.class, unitId, timestamp, userName);
	}

}
