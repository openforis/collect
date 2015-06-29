package org.openforis.collect.event;

import java.util.Date;

public class DoubleAttributeUpdatedEvent extends NumberAttributeUpdatedEvent<Double> {

	public DoubleAttributeUpdatedEvent(String surveyName, Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Double value, Integer unitId, Date timestamp,
			String userName) {
		super(surveyName, recordId, definitionId, parentEntityId, nodeId, value, Double.class, unitId,
				timestamp, userName);
	}

}
