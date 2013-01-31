/**
 * 
 */
package org.openforis.collect.manager.speciesImport;



/**
 * @author S. Ricci
 *
 */
public class TaxonParsingException extends Exception {
	
	public static final long serialVersionUID = 1L;
	
	private TaxonParsingError error;
	
	public TaxonParsingException() {
		super();
	}
	
	public TaxonParsingException(TaxonParsingError error) {
		super();
		this.error = error;
	}
	
	public TaxonParsingException(TaxonParsingError error, Throwable cause) {
		super(cause);
		this.error = error;
	}

	public TaxonParsingError getError() {
		return error;
	}
	
}
