/**
 * 
 */
package org.openforis.collect.exception;

/**
 * @author M. Togna
 * 
 */
public class MissingValueException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MissingValueException() {
		super();
	}

	/**
	 * @param message
	 */
	public MissingValueException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MissingValueException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MissingValueException(String message, Throwable cause) {
		super(message, cause);
	}

}
