/**
 * 
 */
package org.openforis.collect.persistence;

/**
 * @author M. Togna
 * 
 */
public class MultipleEditException extends RecordPersistenceException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MultipleEditException() {
	}

	/**
	 * @param message
	 */
	public MultipleEditException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MultipleEditException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MultipleEditException(String message, Throwable cause) {
		super(message, cause);
	}

}