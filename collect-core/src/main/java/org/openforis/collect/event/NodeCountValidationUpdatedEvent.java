package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

import org.openforis.idm.metamodel.validation.ValidationResultFlag;

public abstract class NodeCountValidationUpdatedEvent extends RecordEvent  {

	private int childDefinitionId;
	private ValidationResultFlag flag;

	public NodeCountValidationUpdatedEvent(String surveyName, Integer recordId, RecordStep recordStep, String definitionId,
			List<String> ancestorIds, String nodeId, Date timestamp, String userName, int childDefinitionId,
			ValidationResultFlag flag) {
		super(surveyName, recordId, recordStep, definitionId, ancestorIds, nodeId, timestamp, userName);
		this.childDefinitionId = childDefinitionId;
		this.flag = flag;
	}
	
	public int getChildDefinitionId() {
		return childDefinitionId;
	}
	
	public ValidationResultFlag getFlag() {
		return flag;
	}
	
}
