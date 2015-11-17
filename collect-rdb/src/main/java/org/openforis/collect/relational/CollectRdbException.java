package org.openforis.collect.relational;

/**
 * 
 * @author G. Miceli
 *
 */
public class CollectRdbException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CollectRdbException() {
		super();
	}

	public CollectRdbException(String message, Throwable cause) {
		super(message, cause);
	}

	public CollectRdbException(String message) {
		super(message);
	}

	public CollectRdbException(Throwable cause) {
		super(cause);
	}
	
}
