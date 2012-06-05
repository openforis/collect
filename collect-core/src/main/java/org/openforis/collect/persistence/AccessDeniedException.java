/**
 * 
 */
package org.openforis.collect.persistence;

/**
 * @author M. Togna
 * 
 */
public class AccessDeniedException extends RecordPersistenceException {

	private static final long serialVersionUID = 1L;

	public AccessDeniedException() {
	}

	public AccessDeniedException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AccessDeniedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AccessDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

}
