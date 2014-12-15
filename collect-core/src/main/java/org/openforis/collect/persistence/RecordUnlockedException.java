/**
 * 
 */
package org.openforis.collect.persistence;

/**
 * @author S. Ricci
 * 
 */
public class RecordUnlockedException extends RecordPersistenceException {

	private static final long serialVersionUID = 1L;

	private String userName;

	public RecordUnlockedException() {
		super();
	}
	
	public RecordUnlockedException(String userName) {
		super();
		this.userName = userName;
	}

	public RecordUnlockedException(String message, String userName) {
		super(message);
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

}
