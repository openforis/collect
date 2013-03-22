package org.openforis.collect.manager.referencedataimport.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;

/**
 * 
 * @author S. Ricci
 *
 */
public class ParsingErrorProxy implements Proxy {

	private transient ParsingError error;

	public ParsingErrorProxy(ParsingError error) {
		super();
		this.error = error;
	}
	
	public static List<ParsingErrorProxy> fromList(List<ParsingError> items) {
		List<ParsingErrorProxy> result = new ArrayList<ParsingErrorProxy>();
		if ( items != null ) {
			for (ParsingError item : items) {
				ParsingErrorProxy proxy = new ParsingErrorProxy(item);
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
	public String[] getColumns() {
		return error.getColumns();
	}

	@ExternalizedProperty
	public String getMessage() {
		return error.getMessage();
	}

	@ExternalizedProperty
	public String[] getMessageArgs() {
		return error.getMessageArgs();
	}
	
}
