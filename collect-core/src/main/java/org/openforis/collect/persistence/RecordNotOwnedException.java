/**
 * 
 */
package org.openforis.collect.persistence;

/**
 * @author S. Ricci
 * 
 */
public class RecordNotOwnedException extends RecordPersistenceException {

	private static final long serialVersionUID = 1L;

	private String ownerName;
	
	public RecordNotOwnedException() {
		super();
	}

	public RecordNotOwnedException(String ownerName) {
		super();
		this.ownerName = ownerName;
	}

	public RecordNotOwnedException(String message, String ownerName) {
		super(message);
		this.ownerName = ownerName;
	}

	public String getOwnerName() {
		return ownerName;
	}

}
