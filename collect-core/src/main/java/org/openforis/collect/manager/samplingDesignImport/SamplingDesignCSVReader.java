/**
 * 
 */
package org.openforis.collect.manager.samplingDesignImport;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.referenceDataImport.CSVDataImportReader;
import org.openforis.collect.manager.referenceDataImport.CSVLineParser;
import org.openforis.collect.manager.referenceDataImport.ParsingError;
import org.openforis.collect.manager.referenceDataImport.ParsingException;
import org.openforis.collect.manager.referenceDataImport.ParsingError.ErrorType;
import org.openforis.commons.io.csv.CsvLine;

/**
 * @author S. Ricci
 *
 */
public class SamplingDesignCSVReader extends CSVDataImportReader<SamplingDesignLine> {

	public static final SamplingDesignFileColumn[] LEVEL_COLUMNS = {SamplingDesignFileColumn.LEVEL_1, SamplingDesignFileColumn.LEVEL_2, SamplingDesignFileColumn.LEVEL_3};

	public SamplingDesignCSVReader(Reader reader) throws IOException, ParsingException {
		super(reader);
	}

	public SamplingDesignCSVReader(String filename) throws IOException, ParsingException {
		super(filename);
	}

	@Override
	protected SamplingDesignCSVLineParser createLineParserInstance() {
		SamplingDesignCSVLineParser lineParser = SamplingDesignCSVLineParser.createInstance(this, currentCSVLine);
		return lineParser;
	}

	@Override
	public boolean validateAllFile() throws ParsingException {
		Validator validator = new Validator();
		validator.validate();
		return true;
	}

	public static class SamplingDesignCSVLineParser extends CSVLineParser<SamplingDesignLine> {

		SamplingDesignCSVLineParser(SamplingDesignCSVReader reader, CsvLine line) {
			super(reader, line);
		}
		
		public static SamplingDesignCSVLineParser createInstance(SamplingDesignCSVReader reader, CsvLine line) {
			return new SamplingDesignCSVLineParser(reader, line);
		}
	
		public SamplingDesignLine parse() throws ParsingException {
			SamplingDesignLine line = super.parse();
			line.setX(getColumnValue(SamplingDesignFileColumn.X.getName(), true, String.class));
			line.setY(getColumnValue(SamplingDesignFileColumn.Y.getName(), true, String.class));
			line.setSrsId(getColumnValue(SamplingDesignFileColumn.SRS_ID.getName(), true, String.class));
			List<String> levelCodes = parseLevelCodes(line);
			line.setLevelCodes(levelCodes);
			return line;
		}

		protected List<String> parseLevelCodes(SamplingDesignLine line)
				throws ParsingException {
			List<String> levelCodes = new ArrayList<String>();
			for (int i = 0; i < LEVEL_COLUMNS.length; i++) {
				SamplingDesignFileColumn column = LEVEL_COLUMNS[i];
				String value = getColumnValue(column.getName(), false, String.class);
				if ( StringUtils.isNotBlank(value) ) {
					if ( i == levelCodes.size() ) {
						levelCodes.add(value);
					} else {
						String previousColumnName = LEVEL_COLUMNS[i-1].getName();
						ParsingError error = new ParsingError(ErrorType.EMPTY, line.getLineNumber(), previousColumnName);
						throw new ParsingException(error);
					}
				} else if ( i == 0 ) {
					ParsingError error = new ParsingError(ErrorType.EMPTY, line.getLineNumber(), column.getName());
					throw new ParsingException(error);
				}
			}
			return levelCodes;
		}
	}
	
	class Validator {
		
		public void validate() throws ParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws ParsingException {
			List<String> colNames = getColumnNames();
			SamplingDesignFileColumn[] expectedColumns = SamplingDesignFileColumn.values();
			int fixedColsSize = expectedColumns.length;
			if ( colNames == null || colNames.size() < fixedColsSize ) {
				ParsingError error = new ParsingError(ErrorType.UNEXPECTED_COLUMNS);
				throw new ParsingException(error);
			}
			for (int i = 0; i < fixedColsSize; i++) {
				String colName = StringUtils.trimToEmpty(colNames.get(i));
				String expectedColName = expectedColumns[i].getName();
				if ( ! expectedColName.equals(colName) ) {
					ParsingError error = new ParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName, expectedColName);
					throw new ParsingException(error);
				}
			}
		}

	}

}
