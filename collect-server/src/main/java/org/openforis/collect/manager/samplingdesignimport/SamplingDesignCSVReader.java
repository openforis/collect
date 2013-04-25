/**
 * 
 */
package org.openforis.collect.manager.samplingdesignimport;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.referencedataimport.CSVDataImportReader;
import org.openforis.collect.manager.referencedataimport.CSVLineParser;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingException;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
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
			line.setX(getColumnValue(SamplingDesignFileColumn.X.getColumnName(), true, String.class));
			line.setY(getColumnValue(SamplingDesignFileColumn.Y.getColumnName(), true, String.class));
			line.setSrsId(getColumnValue(SamplingDesignFileColumn.SRS_ID.getColumnName(), true, String.class));
			List<String> levelCodes = parseLevelCodes(line);
			line.setLevelCodes(levelCodes);
			return line;
		}

		protected List<String> parseLevelCodes(SamplingDesignLine line)
				throws ParsingException {
			List<String> levelCodes = new ArrayList<String>();
			for (int i = 0; i < LEVEL_COLUMNS.length; i++) {
				SamplingDesignFileColumn column = LEVEL_COLUMNS[i];
				String value = getColumnValue(column.getColumnName(), false, String.class);
				if ( StringUtils.isNotBlank(value) ) {
					if ( i == levelCodes.size() ) {
						levelCodes.add(value);
					} else {
						String previousColumnName = LEVEL_COLUMNS[i-1].getColumnName();
						ParsingError error = new ParsingError(ErrorType.EMPTY, line.getLineNumber(), previousColumnName);
						throw new ParsingException(error);
					}
				} else if ( i == 0 ) {
					ParsingError error = new ParsingError(ErrorType.EMPTY, line.getLineNumber(), column.getColumnName());
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
			String[] requiredColumnNames = SamplingDesignFileColumn.REQUIRED_COLUMN_NAMES;
			for (String requiredColumnName : requiredColumnNames) {
				if ( ! colNames.contains(requiredColumnName) ) {
					ParsingError error = new ParsingError(ErrorType.MISSING_REQUIRED_COLUMNS, 1, (String) null);
					String messageArg = StringUtils.join(requiredColumnNames, ", ");
					error.setMessageArgs(new String[]{messageArg});
					throw new ParsingException(error);
				}
			}
		}

	}

}
