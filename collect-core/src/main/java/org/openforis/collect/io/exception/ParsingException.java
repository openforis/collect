/**
 * 
 */
package org.openforis.collect.io.exception;

import org.openforis.collect.io.metadata.parsing.ParsingError;



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
		super(error.getMessage());
		this.error = error;
	}
	
	public ParsingException(ParsingError error, Throwable cause) {
		super(error.getMessage(), cause);
		this.error = error;
	}

	public ParsingError getError() {
		return error;
	}
	
}
