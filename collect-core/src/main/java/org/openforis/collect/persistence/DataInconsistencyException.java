package org.openforis.collect.persistence;

/**
 * @author G. Miceli
 */
public class DataInconsistencyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DataInconsistencyException(String message) {
		super(message);
	}

}
