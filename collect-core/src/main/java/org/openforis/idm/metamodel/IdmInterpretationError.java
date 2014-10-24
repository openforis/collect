package org.openforis.idm.metamodel;

/**
 * @author G. Miceli
 */
public class IdmInterpretationError extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public IdmInterpretationError() {
		super();
	}

	public IdmInterpretationError(String message, Throwable cause) {
		super(message, cause);
	}

	public IdmInterpretationError(String message) {
		super(message);
	}

	public IdmInterpretationError(Throwable cause) {
		super(cause);
	}
}
