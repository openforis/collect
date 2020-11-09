/**
 * 
 */
package org.openforis.collect.io.metadata.parsing;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.commons.io.csv.CsvLine;

/**
 * @author S. Ricci
 *
 */
public abstract class CSVLineParser<T extends Line> extends LineParser<T> {

	protected CsvLine csvLine;
	protected DataImportReader<T> reader;
	
	public CSVLineParser(DataImportReader<T> reader,
			CsvLine csvLine) {
		super(reader.getLinesRead() + 1);
		this.reader = reader;
		this.csvLine = csvLine;
	}
	
	@Override
	protected <V> V getColumnValue(String column, boolean required, Class<V> type) throws ParsingException {
		return getColumnValue(column, type, required, null);
	}

	protected <V> V getColumnValue(String column, Class<V> type, boolean required, Integer maxLength) throws ParsingException {
		V value = getColumnValue(column, type);
		if ( required && ( value == null || value instanceof String && StringUtils.isBlank((String) value) )) {
			throwEmptyColumnParsingException(column);
		}
		if (maxLength != null && value != null && value instanceof String && ((String) value).length() > maxLength) {
			throwMaxLengthExceededParsingException(column, maxLength);
		}
		if ( value instanceof String ) {
			value = trimValue(value);
		}
		return value;
	}
	
	private <V> V getColumnValue(String column, Class<V> type) {
		String[] lineContent = csvLine.getLine();
		Integer columnIndex = csvLine.getColumnIndex(column);
		if ( columnIndex == null || columnIndex < 0 || columnIndex >= lineContent.length ) {
			return null;
		} else {
			return csvLine.getValue(columnIndex, type);
		}
	}

	@SuppressWarnings("unchecked")
	protected <V> V trimValue(V value) {
		return (V) ((String) value).trim();
	}
	
	private void throwMaxLengthExceededParsingException(String column, int maxLength) throws ParsingException {
		ParsingError error = new ParsingError(ErrorType.MAX_LENGTH_EXCEEDED, lineNumber, column);
		error.setMessageArgs(new String[] {String.valueOf(maxLength)});
		throw new ParsingException(error);
	}
	
	public DataImportReader<T> getReader() {
		return reader;
	}

}
