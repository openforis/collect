package org.openforis.collect.manager.referencedataimport.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;

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
		if (items != null) {
			for (ParsingError item : items) {
				ParsingErrorProxy proxy = new ParsingErrorProxy(item);
				result.add(proxy);
			}
		}
		return result;
	}

	public ErrorType getErrorType() {
		return error.getErrorType();
	}

	public long getRow() {
		return error.getRow();
	}

	public String[] getColumns() {
		return error.getColumns();
	}

	public String getMessage() {
		return error.getMessage();
	}

	public String[] getMessageArgs() {
		return error.getMessageArgs();
	}

}
