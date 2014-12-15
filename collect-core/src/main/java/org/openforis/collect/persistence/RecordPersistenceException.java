package org.openforis.collect.persistence;

/**
 * @author S. Ricci
 * 
 */
public class RecordPersistenceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RecordPersistenceException() {
		super();
	}
	
	/**
	 * @param message
	 */
	public RecordPersistenceException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RecordPersistenceException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RecordPersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	
}
