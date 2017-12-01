package org.openforis.collect.command;

public class AddEntityCommand extends AddNodeCommand {

	private static final long serialVersionUID = 1L;
	
	private int index = 0;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
}
