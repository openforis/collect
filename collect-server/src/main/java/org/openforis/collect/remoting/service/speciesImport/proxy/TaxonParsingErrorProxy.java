package org.openforis.collect.remoting.service.speciesImport.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.manager.speciesImport.TaxonParsingError;
import org.openforis.collect.manager.speciesImport.TaxonParsingError.ErrorType;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonParsingErrorProxy implements Proxy {

	private transient TaxonParsingError error;

	public TaxonParsingErrorProxy(TaxonParsingError error) {
		super();
		this.error = error;
	}
	
	public static List<TaxonParsingErrorProxy> fromList(List<TaxonParsingError> items) {
		List<TaxonParsingErrorProxy> result = new ArrayList<TaxonParsingErrorProxy>();
		if ( items != null ) {
			for (TaxonParsingError item : items) {
				TaxonParsingErrorProxy proxy = new TaxonParsingErrorProxy(item);
				result.add(proxy);
			}
		}
		return result;
	}

	@ExternalizedProperty
	public ErrorType getErrorType() {
		return error.getErrorType();
	}

	@ExternalizedProperty
	public long getRow() {
		return error.getRow();
	}

	@ExternalizedProperty
	public String getColumn() {
		return error.getColumn();
	}

	@ExternalizedProperty
	public String getMessage() {
		return error.getMessage();
	}
	
}
