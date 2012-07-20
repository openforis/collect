/**
 * 
 */
package org.openforis.collect.manager;

/**
 * @author riccist
 *
 */
public class RecordDataIndexException extends Exception {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public RecordDataIndexException() {
	}

	/**
	 * @param message
	 */
	public RecordDataIndexException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RecordDataIndexException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RecordDataIndexException(String message, Throwable cause) {
		super(message, cause);
	}

}
