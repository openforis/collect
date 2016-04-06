/**
 * 
 */
package org.openforis.collect.web.session;

/**
 * @author M. Togna
 *
 */
public class InvalidSessionException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public InvalidSessionException() {
	}
	
	public InvalidSessionException(String message) {
		super(message);
	}

}
