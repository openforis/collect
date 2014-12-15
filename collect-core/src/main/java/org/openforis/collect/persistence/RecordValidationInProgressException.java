/**
 * 
 */
package org.openforis.collect.persistence;

/**
 * @author S. Ricci
 * 
 */
public class RecordValidationInProgressException extends RecordLockedException {

	private static final long serialVersionUID = 1L;

	public RecordValidationInProgressException() {
		super();
	}
	
	public RecordValidationInProgressException(String message) {
		super(message);
	}

}
