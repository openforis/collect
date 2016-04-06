package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class BooleanAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final Boolean value;

	public BooleanAttributeUpdatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, Boolean value, Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				timestamp, userName);
		this.value = value;
	}

	public Boolean getValue() {
		return value;
	}

}
