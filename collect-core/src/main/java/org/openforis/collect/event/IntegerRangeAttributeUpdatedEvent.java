package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class IntegerRangeAttributeUpdatedEvent extends
		RangeAttributeUpdatedEvent<Integer> {

	public IntegerRangeAttributeUpdatedEvent(String surveyName,
			Integer recordId, RecordStep step, String definitionId,
			List<String> ancestorIds, String nodeId, Integer from, Integer to,
			Integer unitId, Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				from, to, Integer.class, unitId, timestamp, userName);
	}

}
