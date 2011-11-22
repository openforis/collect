/**
 * 
 */
package org.openforis.collect.exception;

/**
 * @author Mino Togna
 * 
 */
public class OperationNotPermittedException extends Exception {

	private static final long serialVersionUID = 1L;

	public static enum Reason {
		RECORD_LOCKED, ACTIVE_RECORD_ALREADY_EXIST, ACCESS_DENIDED;
	}

	private String reason;

	public OperationNotPermittedException(Reason reason) {
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
