package org.openforis.collect.event;

import org.openforis.idm.metamodel.validation.ValidationResultFlag;

public abstract class NodeCountValidationUpdatedEvent extends RecordEvent  {

	private int childDefinitionId;
	private ValidationResultFlag flag;

	public int getChildDefinitionId() {
		return childDefinitionId;
	}
	
	public void setChildDefinitionId(int childDefinitionId) {
		this.childDefinitionId = childDefinitionId;
	}
	
	public ValidationResultFlag getFlag() {
		return flag;
	}
	
	public void setFlag(ValidationResultFlag flag) {
		this.flag = flag;
	}
	
}
