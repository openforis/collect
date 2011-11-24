/**
 * 
 */
package org.openforis.collect.exception;

/**
 * @author Mino Togna
 * 
 */
public class RecordLockedException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public RecordLockedException() {
	}

	/**
	 * @param message
	 */
	public RecordLockedException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RecordLockedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RecordLockedException(String message, Throwable cause) {
		super(message, cause);
	}

}
