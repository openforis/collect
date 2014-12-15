package org.openforis.collect.persistence;

/**
 * @author G. Miceli
 */
public class DataInconsistencyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DataInconsistencyException() {
		super();
	}

	public DataInconsistencyException(String message) {
		super(message);
	}

	public DataInconsistencyException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataInconsistencyException(Throwable cause) {
		super(cause);
	}
	
	

}
