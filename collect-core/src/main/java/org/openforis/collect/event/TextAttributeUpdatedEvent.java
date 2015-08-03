package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class TextAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final String text;

	public TextAttributeUpdatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, String text, Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				timestamp, userName);
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
