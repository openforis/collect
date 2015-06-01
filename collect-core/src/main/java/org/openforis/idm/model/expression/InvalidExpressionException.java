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
	private String detailedMessage;

	public InvalidExpressionException(String message) {
		this(message, null);
	}
	
	public InvalidExpressionException(String message, String expression) {
		this(message, null, null);
	}

	public InvalidExpressionException(String message, String expression, String detailedMessage) {
		super(message);
		this.expression = expression;
		this.detailedMessage = detailedMessage;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public String getDetailedMessage() {
		return detailedMessage;
	}
}