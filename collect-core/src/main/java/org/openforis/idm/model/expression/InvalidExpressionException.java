/**
 * 
 */
package org.openforis.idm.model.expression;

/**
 * @author M. Togna
 * 
 */
public class InvalidExpressionException extends Exception {

	private static final long serialVersionUID = 1L;
	private String expression;

	public InvalidExpressionException(String message) {
		this(message, null);
	}
	
	public InvalidExpressionException(String message, String expression) {
		super(message);
		this.expression = expression;
	}
	
	public String getExpression() {
		return expression;
	}

}