/**
 * 
 */
package org.openforis.idm.model.expression;

/**
 * @author S. Ricci
 * 
 */
public class ReferenceLoopException extends Exception {

	private static final long serialVersionUID = 1L;

	public ReferenceLoopException(String message) {
		super(message);
	}

}