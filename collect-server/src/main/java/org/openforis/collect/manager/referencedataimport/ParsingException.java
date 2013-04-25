/**
 * 
 */
package org.openforis.collect.manager.referencedataimport;



/**
 * @author S. Ricci
 *
 */
public class ParsingException extends Exception {
	
	public static final long serialVersionUID = 1L;
	
	private ParsingError error;
	
	public ParsingException() {
		super();
	}
	
	public ParsingException(Throwable t) {
		super(t);
	}
	
	public ParsingException(ParsingError error) {
		super();
		this.error = error;
	}
	
	public ParsingException(ParsingError error, Throwable cause) {
		super(cause);
		this.error = error;
	}

	public ParsingError getError() {
		return error;
	}
	
}
