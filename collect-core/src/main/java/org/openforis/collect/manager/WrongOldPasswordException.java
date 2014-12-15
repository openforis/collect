/**
 * 
 */
package org.openforis.collect.manager;

/**
 * @author S. Ricci
 *
 */
public class WrongOldPasswordException extends UserPersistenceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public WrongOldPasswordException() {
		super("Wrong old password specified");
	}

}
