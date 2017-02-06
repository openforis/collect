package org.openforis.collect.command;

import java.util.Date;

public class UpdateDateAttributeCommand extends UpdateAttributeCommand {

	private static final long serialVersionUID = 1L;
	
	private Date value;

	public UpdateDateAttributeCommand() {
	}
	
	public Date getValue() {
		return value;
	}
	
	public void setValue(Date value) {
		this.value = value;
	}
	
}
