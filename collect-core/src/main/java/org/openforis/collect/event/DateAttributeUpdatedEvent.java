package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class DateAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final Date date;

	public DateAttributeUpdatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, Date date, Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				timestamp, userName);
		this.date = date;
	}

	public Date getDate() {
		return date;
	}
}
