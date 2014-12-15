/**
 * 
 */
package org.openforis.collect.manager;

/**
 * @author S. Ricci
 *
 */
public class InvalidUserPasswordException extends UserPersistenceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public InvalidUserPasswordException() {
		super("Invalid user password specified");
	}

}
