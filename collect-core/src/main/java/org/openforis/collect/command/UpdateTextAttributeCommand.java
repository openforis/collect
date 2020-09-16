package org.openforis.collect.command;

public class UpdateTextAttributeCommand extends UpdateAttributeCommand {

	private static final long serialVersionUID = 1L;
	
	private String value;

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
}
