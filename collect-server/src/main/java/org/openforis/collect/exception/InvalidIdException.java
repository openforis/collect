/**
 * 
 */
package org.openforis.collect.exception;

/**
 * @author Mino Togna
 * 
 */
public class InvalidIdException extends Exception {

	public static enum Reason {
		DUPLICATE, INVALID, NOT_EXIST
	}

	private static final long serialVersionUID = 1L;

	private String reason;

	public InvalidIdException(Reason reason) {
		super();
		this.setReason(reason.toString());
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
