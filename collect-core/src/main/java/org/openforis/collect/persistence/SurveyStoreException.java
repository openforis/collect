package org.openforis.collect.persistence;

/**
 * @author G. Miceli
 */
public class SurveyStoreException extends Exception {

	private static final long serialVersionUID = 1L;

	public SurveyStoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public SurveyStoreException(String message) {
		super(message);
	}

	public SurveyStoreException(Throwable cause) {
		super(cause);
	}
	
}
