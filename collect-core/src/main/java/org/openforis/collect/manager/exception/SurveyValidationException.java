/**
 * 
 */
package org.openforis.collect.manager.exception;

/**
 * @author S. Ricci
 *
 */
public class SurveyValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	public SurveyValidationException() {
		super();
	}

	public SurveyValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SurveyValidationException(String message) {
		super(message);
	}

	public SurveyValidationException(Throwable cause) {
		super(cause);
	}
	
}
