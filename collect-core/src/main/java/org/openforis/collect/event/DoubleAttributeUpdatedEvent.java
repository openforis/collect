package org.openforis.collect.event;

import java.util.Date;

public class DoubleAttributeUpdatedEvent extends NumericAttributeUpdatedEvent<Double> {

	public DoubleAttributeUpdatedEvent(Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Double value, Integer unitId, Date timestamp,
			String userName) {
		super(recordId, definitionId, parentEntityId, nodeId, value, Double.class, unitId,
				timestamp, userName);
	}

}
