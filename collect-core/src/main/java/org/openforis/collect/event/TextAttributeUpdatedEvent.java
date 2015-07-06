package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class TextAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final String text;
	
	public TextAttributeUpdatedEvent(String surveyName, Integer recordId, String definitionId, List<String> ancestorIds, 
			String nodeId, String text, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, ancestorIds, nodeId, timestamp, userName);
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
}
