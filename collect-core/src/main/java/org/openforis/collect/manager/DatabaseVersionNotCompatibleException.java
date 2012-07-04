package org.openforis.collect.manager;

/**
 * 
 * @author S. Ricci
 *
 */
public class DatabaseVersionNotCompatibleException extends Exception {

	private static final long serialVersionUID = 1L;

	public DatabaseVersionNotCompatibleException() {
		super();
	}

	public DatabaseVersionNotCompatibleException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseVersionNotCompatibleException(String message) {
		super(message);
	}

	public DatabaseVersionNotCompatibleException(Throwable cause) {
		super(cause);
	}

}
