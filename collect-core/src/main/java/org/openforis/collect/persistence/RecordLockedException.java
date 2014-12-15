/**
 * 
 */
package org.openforis.collect.persistence;

/**
 * @author M. Togna
 * 
 */
public class RecordLockedException extends RecordPersistenceException {

	private static final long serialVersionUID = 1L;

	private String userName;
	
	public RecordLockedException() {
		super();
	}

	public RecordLockedException(String userName) {
		super();
		this.userName = userName;
	}

	public RecordLockedException(String message, String userName) {
		super(message);
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

}
