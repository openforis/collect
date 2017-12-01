package org.openforis.collect.command;

public abstract class AddNodeCommand extends NodeCommand {

	private static final long serialVersionUID = 1L;
	
	private int index = 0;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
}
