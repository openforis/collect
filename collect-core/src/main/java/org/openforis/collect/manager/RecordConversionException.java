package org.openforis.collect.manager;

/**
 * @author S. Ricci
 *
 */
public class RecordConversionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RecordConversionException() {
		super();
	}

	protected RecordConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	protected RecordConversionException(String message) {
		super(message);
	}

	protected RecordConversionException(Throwable cause) {
		super(cause);
	}

}
