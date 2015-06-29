package org.openforis.collect.event;

import java.util.Date;

public class CodeAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private final String code;
	private final String qualifier;
	
	public CodeAttributeUpdatedEvent(String surveyName, Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, String code, String qualifier, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
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
