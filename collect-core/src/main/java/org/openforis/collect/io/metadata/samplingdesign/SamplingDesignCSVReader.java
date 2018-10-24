/**
 * 
 */
package org.openforis.collect.io.metadata.samplingdesign;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.CSVReferenceDataImportReader;
import org.openforis.collect.io.metadata.parsing.CSVReferenceDataLineParser;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignLine.SamplingDesignLineCodeKey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;
import org.openforis.commons.io.csv.CsvLine;

/**
 * @author S. Ricci
 *
 */
public class SamplingDesignCSVReader extends CSVReferenceDataImportReader<SamplingDesignLine> {

	public SamplingDesignCSVReader(File file) throws IOException, ParsingException {
		super(file);
	}
	
	@Override
	protected boolean isInfoAttribute(String col) {
		for (SamplingDesignFileColumn column : SamplingDesignFileColumn.values()) {
			if ( column.getColumnName().equalsIgnoreCase(col) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected SamplingDesignCSVLineParser createLineParserInstance() {
		SamplingDesignCSVLineParser lineParser = SamplingDesignCSVLineParser.createInstance(this, currentCSVLine, infoColumnNames);
		return lineParser;
	}

	@Override
	public boolean validateAllFile() throws ParsingException {
		Validator validator = new Validator();
		validator.validate();
		return true;
	}

	private static class SamplingDesignCSVLineParser extends CSVReferenceDataLineParser<SamplingDesignLine> {

		SamplingDesignCSVLineParser(SamplingDesignCSVReader reader, CsvLine line, List<String> infoColumnNames) {
			super(reader, line, infoColumnNames);
		}
		
		public static SamplingDesignCSVLineParser createInstance(SamplingDesignCSVReader reader, 
				CsvLine line, List<String> infoColumnNames) {
			return new SamplingDesignCSVLineParser(reader, line, infoColumnNames);
		}
	
		public SamplingDesignLine parse() throws ParsingException {
			SamplingDesignLine line = super.parse();
			line.setX(getColumnValue(SamplingDesignFileColumn.X.getColumnName(), true, String.class));
			line.setY(getColumnValue(SamplingDesignFileColumn.Y.getColumnName(), true, String.class));
			line.setSrsId(getColumnValue(SamplingDesignFileColumn.SRS_ID.getColumnName(), true, String.class));
			List<String> levelCodes = parseLevelCodes(line);
			line.setKey(new SamplingDesignLineCodeKey(levelCodes));
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
	}
	
	
	class Validator {
		
		private static final int MAX_INFO_COLUMNS = 30;

		public void validate() throws ParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws ParsingException {
			List<String> colNames = getColumnNames();
			String[] requiredColumnNames = SamplingDesignFileColumn.REQUIRED_COLUMN_NAMES;
			for (String requiredColumnName : requiredColumnNames) {
				if ( ! colNames.contains(requiredColumnName) ) {
					ParsingError error = new ParsingError(ErrorType.MISSING_REQUIRED_COLUMNS, 1);
					String messageArg = StringUtils.join(requiredColumnNames, ", ");
					error.setMessageArgs(new String[]{messageArg});
					throw new ParsingException(error);
				}
			}
			final List<String> requiredColumnNamesList = Arrays.asList(requiredColumnNames);
			List<String> infoColumns = new ArrayList<String>(colNames);
			CollectionUtils.filter(infoColumns, new Predicate<String>() {
				public boolean evaluate(String item) {
					return !requiredColumnNamesList.contains(item);
				}
			});
			if (infoColumns.size() > MAX_INFO_COLUMNS) {
				ParsingError error = new ParsingError(ErrorType.EXCEEDING_MAXIMUM_EXTRA_COLUMNS, 1);
				error.setMessageArgs(new String[]{
						String.valueOf(MAX_INFO_COLUMNS), String.valueOf(infoColumns.size())});
				throw new ParsingException(error);
			}
		}

	}

}
