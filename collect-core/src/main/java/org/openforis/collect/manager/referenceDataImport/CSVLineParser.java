/**
 * 
 */
package org.openforis.collect.manager.referenceDataImport;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.io.csv.CsvLine;

/**
 * @author S. Ricci
 *
 */
public abstract class CSVLineParser<T extends Line> extends LineParser<T> {

	private CsvLine csvLine;
	
	public CSVLineParser(DataImportReader<T> reader,
			CsvLine csvLine) {
		super(reader.getLinesRead() + 1);
		this.csvLine = csvLine;
	}
	
	@Override
	protected <V> V getColumnValue(String column, boolean required, Class<V> type) throws ParsingException {
		V value = csvLine.getValue(column, type);
		if ( required && ( value == null || value instanceof String && StringUtils.isBlank((String) value) )) {
			throwEmptyColumnParsingException(column);
		}
		return value;
	}
	

}
