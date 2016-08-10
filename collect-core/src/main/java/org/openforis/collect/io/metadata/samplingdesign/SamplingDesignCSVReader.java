/**
 * 
 */
package org.openforis.collect.io.metadata.samplingdesign;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.CSVDataImportReader;
import org.openforis.collect.io.metadata.parsing.CSVLineParser;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignLine.SamplingDesignLineCodeKey;
import org.openforis.collect.utils.SurveyObjects;
import org.openforis.commons.io.csv.CsvLine;

/**
 * @author S. Ricci
 *
 */
public class SamplingDesignCSVReader extends CSVDataImportReader<SamplingDesignLine> {

	private List<String> infoColumnNames;
	
	public SamplingDesignCSVReader(File file) throws IOException, ParsingException {
		super(file);
		this.infoColumnNames = new ArrayList<String>();
	}
	
	@Override
	public void init() throws IOException, ParsingException {
		super.init();
		List<String> columnNames = csvReader.getColumnNames();
		for (String col : columnNames) {
			String adjustedName = SurveyObjects.adjustInternalName(col);
			if ( isInfoAttribute(adjustedName) ) {
				infoColumnNames.add(adjustedName);
			}
		}
	}

	private boolean isInfoAttribute(String col) {
		for (SamplingDesignFileColumn column : SamplingDesignFileColumn.values()) {
			if ( column.getColumnName().equalsIgnoreCase(col) ) {
				return false;
			}
		}
		return true;
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

	public List<String> getInfoColumnNames() {
		return infoColumnNames;
	}
	
	private static class SamplingDesignCSVLineParser extends CSVLineParser<SamplingDesignLine> {

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
			line.setKey(new SamplingDesignLineCodeKey(levelCodes));
			Map<String, String> infos = parseInfos(line);
			line.setInfoAttributeByName(infos);
			return line;
		}

		protected List<String> parseLevelCodes(SamplingDesignLine line)
				throws ParsingException {
			List<String> levelCodes = new ArrayList<String>();
			for (int i = 0; i < SamplingDesignFileColumn.LEVEL_COLUMNS.length; i++) {
				SamplingDesignFileColumn column = SamplingDesignFileColumn.LEVEL_COLUMNS[i];
				String value = getColumnValue(column.getColumnName(), false, String.class);
				if ( StringUtils.isNotBlank(value) ) {
					if ( i == levelCodes.size() ) {
						levelCodes.add(value);
					} else {
						String previousColumnName = SamplingDesignFileColumn.LEVEL_COLUMNS[i-1].getColumnName();
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
		
		protected Map<String, String> parseInfos(SamplingDesignLine line) throws ParsingException {
			Map<String, String> result = new HashMap<String, String>();
			SamplingDesignCSVReader reader = (SamplingDesignCSVReader) getReader();
			for (String columnName : reader.infoColumnNames ) {
				String value = getColumnValue(columnName, false, String.class);
				if ( StringUtils.isNotBlank(value) ) {
					result.put(columnName, value);
				}
			}
			return result;
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
