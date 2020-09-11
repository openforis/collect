package org.openforis.collect.command;

public abstract class NodeCommand extends RecordCommand {

	private static final long serialVersionUID = 1L;

	private int nodeDefId;
	private String parentEntityPath;
	private String nodePath;

	public int getNodeDefId() {
		return nodeDefId;
	}
	
	public void setNodeDefId(int nodeDefId) {
		this.nodeDefId = nodeDefId;
	}
	
	public String getParentEntityPath() {
		return parentEntityPath;
	}
	
	public void setParentEntityPath(String parentEntityPath) {
		this.parentEntityPath = parentEntityPath;
	}
	
	public String getNodePath() {
		return nodePath;
	}
	
	public void setNodePath(String nodePath) {
		this.nodePath = nodePath;
	}
}