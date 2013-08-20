package org.openforis.collect.persistence;

/**
 * @author G. Miceli
 */
public class SurveyImportException extends Exception {

	private static final long serialVersionUID = 1L;

	public SurveyImportException(String message, Throwable cause) {
		super(message, cause);
	}

	public SurveyImportException(String message) {
		super(message);
	}

	public SurveyImportException(Throwable cause) {
		super(cause);
	}
	
}
