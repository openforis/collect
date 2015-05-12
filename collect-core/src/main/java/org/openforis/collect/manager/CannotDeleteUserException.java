/**
 * 
 */
package org.openforis.collect.manager;

/**
 * @author S. Ricci
 *
 */
public class CannotDeleteUserException extends UserPersistenceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public CannotDeleteUserException() {
		super("Cannot delete user: it has associated records");
	}

}
