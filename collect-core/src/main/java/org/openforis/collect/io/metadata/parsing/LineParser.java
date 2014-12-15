/**
 * 
 */
package org.openforis.collect.io.metadata.parsing;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;


/**
 * @author S. Ricci
 *
 */
public abstract class LineParser<T extends Line> {
	
	protected long lineNumber;

	public LineParser(long lineNumber) {
		super();
		this.lineNumber = lineNumber;
	}
	
	public long getLineNumber() {
		return lineNumber;
	}
	
	public T parse() throws ParsingException {
		T line = newLineInstance();
		line.setLineNumber(getLineNumber());
		return line;
	}

	protected T newLineInstance() throws ParsingException {
		ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
		Type[] actualTypeArguments = type.getActualTypeArguments();
		@SuppressWarnings("unchecked")
		Class<T> lineType = (Class<T>) actualTypeArguments[0];
		T line;
		try {
			line = lineType.newInstance();
		} catch (Exception e) {
			throw new ParsingException(e);
		}
		return line;
	}
	
	protected abstract <V> V getColumnValue(String column, boolean required, Class<V> type) throws ParsingException;
	
	protected ParsingError createFieldParsingError(String column, String fieldName, String value) {
		ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, lineNumber, 
				column, "Error parsing " + fieldName +" from " + value);
		return error;
	}
	
	protected void throwEmptyColumnParsingException(String column)
			throws ParsingException {
		ParsingError error = new ParsingError(ErrorType.EMPTY, lineNumber, column);
		throw new ParsingException(error);
	}
	
}
