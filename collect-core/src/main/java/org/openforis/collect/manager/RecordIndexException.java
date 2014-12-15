/**
 * 
 */
package org.openforis.collect.manager;

/**
 * @author riccist
 *
 */
public class RecordIndexException extends Exception {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public RecordIndexException() {
	}

	/**
	 * @param message
	 */
	public RecordIndexException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RecordIndexException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RecordIndexException(String message, Throwable cause) {
		super(message, cause);
	}

}
