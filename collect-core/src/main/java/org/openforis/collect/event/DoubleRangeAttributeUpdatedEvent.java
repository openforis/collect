package org.openforis.collect.event;

import java.util.Date;

public class DoubleRangeAttributeUpdatedEvent extends RangeAttributeUpdatedEvent<Double> {

	public DoubleRangeAttributeUpdatedEvent(Integer recordId,
			int definitionId, Integer parentEntityId, int nodeId, Double from,
			Double to, Integer unitId,
			Date timestamp, String userName) {
		super(recordId, definitionId, parentEntityId, nodeId, from, to, Double.class,
				unitId, timestamp, userName);
	}

}
