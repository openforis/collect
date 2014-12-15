/**
 * 
 */
package org.openforis.collect.manager.exception;

import org.openforis.collect.persistence.RecordPersistenceException;

/**
 * @author S. Ricci
 *
 */
public class RecordFileException extends RecordPersistenceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public RecordFileException() {
	}

	/**
	 * @param message
	 */
	public RecordFileException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RecordFileException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RecordFileException(String message, Throwable cause) {
		super(message, cause);
	}

}
