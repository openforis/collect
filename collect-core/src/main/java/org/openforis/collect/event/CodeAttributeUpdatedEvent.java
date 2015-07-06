package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class CodeAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final String code;
	private final String qualifier;
	
	public CodeAttributeUpdatedEvent(String surveyName, Integer recordId, String definitionId, 
			List<String> ancestorIds, String nodeId,
			String code, String qualifier, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, ancestorIds, nodeId, timestamp, userName);
		this.code = code;
		this.qualifier = qualifier;
	}

	public String getCode() {
		return code;
	}
	
	public String getQualifier() {
		return qualifier;
	}
}
