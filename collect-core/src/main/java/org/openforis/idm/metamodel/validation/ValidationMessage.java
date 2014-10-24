package org.openforis.idm.metamodel.validation;

public class ValidationMessage {

	public enum Flag {
		ERROR, WARN;
	}

	private Flag flag;
	private String message;
	
	public ValidationMessage(Flag flag, String message) {
		this.flag = flag;
		this.message = message;
	}

	public Flag getFlag() {
		return flag;
	}

	public String getMessage() {
		return message;
	}

}
