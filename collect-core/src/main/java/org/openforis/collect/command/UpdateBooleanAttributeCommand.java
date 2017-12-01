package org.openforis.collect.command;

public class UpdateBooleanAttributeCommand extends UpdateAttributeCommand {

	private static final long serialVersionUID = 1L;
	
	private Boolean value;

	public UpdateBooleanAttributeCommand() {
	}
	
	public Boolean getValue() {
		return value;
	}
	
	public void setValue(Boolean value) {
		this.value = value;
	}
	
}
