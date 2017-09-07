/**
 * 
 */
package org.openforis.collect.manager;

/**
 * @author S. Ricci
 *
 */
public class UserPersistenceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public UserPersistenceException() {
	}

	/**
	 * @param message
	 */
	public UserPersistenceException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UserPersistenceException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UserPersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
