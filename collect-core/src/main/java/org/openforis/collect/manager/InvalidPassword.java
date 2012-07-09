/**
 * 
 */
package org.openforis.collect.manager;

/**
 * @author S. Ricci
 *
 */
public class InvalidPassword extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public InvalidPassword() {
		super("Invalid user password specified");
	}

	/**
	 * @param message
	 */
	public InvalidPassword(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidPassword(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidPassword(String message, Throwable cause) {
		super(message, cause);
	}

}
