/**
 * 
 */
package org.openforis.collect.persistence;

/**
 * @author S. Ricci
 * 
 */
public class RecordLockedByActiveUserException extends RecordLockedException {

	private static final long serialVersionUID = 1L;

	public RecordLockedByActiveUserException(String userName) {
		super(userName);
	}

	public RecordLockedByActiveUserException(String message, String userName) {
		super(message, userName);
	}

}
