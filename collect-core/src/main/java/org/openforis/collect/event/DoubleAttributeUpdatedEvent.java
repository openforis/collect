package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class DoubleAttributeUpdatedEvent extends
		NumberAttributeUpdatedEvent<Double> {

	public DoubleAttributeUpdatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, Double value, Integer unitId, Date timestamp,
			String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				value, Double.class, unitId, timestamp, userName);
	}

}
