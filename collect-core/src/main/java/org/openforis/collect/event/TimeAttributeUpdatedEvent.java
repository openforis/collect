package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class TimeAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final Date time;

	public TimeAttributeUpdatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, Date time, Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				timestamp, userName);
		this.time = time;
	}

	public Date getTime() {
		return time;
	}
}
