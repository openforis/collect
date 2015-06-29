package org.openforis.collect.event;

import java.util.Date;

public class TextAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final String text;
	
	public TextAttributeUpdatedEvent(String surveyName, Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, String text, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
}
