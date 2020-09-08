package org.openforis.collect.event;

public class NodeRelevanceUpdatedEvent extends RecordEvent  {

	private int childDefinitionId;
	private boolean relevant;

	public int getChildDefinitionId() {
		return childDefinitionId;
	}
	
	public void setChildDefinitionId(int childDefinitionId) {
		this.childDefinitionId = childDefinitionId;
	}
	
	public boolean isRelevant() {
		return relevant;
	}

	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
	}
}
