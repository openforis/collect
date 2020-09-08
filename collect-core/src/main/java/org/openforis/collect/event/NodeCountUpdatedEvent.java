package org.openforis.collect.event;

public abstract class NodeCountUpdatedEvent extends RecordEvent {

	private int childDefinitionId;
	private int count;

	public int getChildDefinitionId() {
		return childDefinitionId;
	}

	public void setChildDefinitionId(int childDefinitionId) {
		this.childDefinitionId = childDefinitionId;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
}
