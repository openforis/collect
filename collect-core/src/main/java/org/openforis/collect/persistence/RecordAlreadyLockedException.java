/**
 * 
 */
package org.openforis.collect.persistence;

/**
 * @author S. Ricci
 * 
 */
public class RecordAlreadyLockedException extends RecordLockedException {

	private static final long serialVersionUID = 1L;

	public RecordAlreadyLockedException(String userName) {
		super(userName);
	}

	public RecordAlreadyLockedException(String message, String userName) {
		super(message, userName);
	}

}
