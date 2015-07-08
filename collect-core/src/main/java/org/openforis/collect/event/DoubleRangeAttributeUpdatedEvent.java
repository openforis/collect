package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class DoubleRangeAttributeUpdatedEvent extends
		RangeAttributeUpdatedEvent<Double> {

	public DoubleRangeAttributeUpdatedEvent(String surveyName,
			Integer recordId, RecordStep step, String definitionId,
			List<String> ancestorIds, String nodeId, Double from, Double to,
			Integer unitId, Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				from, to, Double.class, unitId, timestamp, userName);
	}

}
