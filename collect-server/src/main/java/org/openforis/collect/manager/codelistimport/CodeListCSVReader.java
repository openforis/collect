/**
 * 
 */
package org.openforis.collect.manager.codelistimport;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.referencedataimport.CSVDataImportReader;
import org.openforis.collect.manager.referencedataimport.CSVLineParser;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.referencedataimport.ParsingException;
import org.openforis.commons.io.csv.CsvLine;

/**
 * @author S. Ricci
 *
 */
public class CodeListCSVReader extends CSVDataImportReader<CodeListLine> {

	public static final String CODE_COLUMN_SUFFIX = "_code";
	public static final String LEVEL_NAME_COLUMN_EXPR = "[a-z][a-z0-9_]*";
	public static final String CODE_COLUMN_EXPR = "^" + LEVEL_NAME_COLUMN_EXPR + CODE_COLUMN_SUFFIX + "$";
	public static final String LABEL_COLUMN_EXPR = "^" + LEVEL_NAME_COLUMN_EXPR + "$";

	private List<String> levels;
	
	public CodeListCSVReader(String filename) throws IOException, ParsingException {
		super(filename);
		initLevels();
	}

	public CodeListCSVReader(Reader reader) throws IOException, ParsingException {
		super(reader);
		initLevels();
	}
	
	@Override
	protected CodeListCSVLineParser createLineParserInstance() {
		CodeListCSVLineParser lineParser = CodeListCSVLineParser.createInstance(this, currentCSVLine);
		return lineParser;
	}
	

	@Override
	public boolean validateAllFile() throws ParsingException {
		Validator validator = new Validator();
		validator.validate();
		return true;
	}
	
	public void initLevels() {
		List<String> colNames = csvReader.getColumnNames();
		levels = new ArrayList<String>();
		for (int i = 0; i < colNames.size(); i++) {
			String colName = colNames.get(i);
			if ( i % 2 != 0 ) {
				levels.add(colName);
			}
		}
	}
	
	public List<String> getLevels() {
		return levels;
	}
	
	public static class CodeListCSVLineParser extends CSVLineParser<CodeListLine> {
		
		CodeListCSVLineParser(CodeListCSVReader reader, CsvLine line) {
			super(reader, line);
		}
		
		public static CodeListCSVLineParser createInstance(CodeListCSVReader reader, CsvLine line) {
			return new CodeListCSVLineParser(reader, line);
		}
	
		public CodeListLine parse() throws ParsingException {
			CodeListLine line = super.parse();
			List<String> levels = ((CodeListCSVReader) reader).getLevels();
			for (String level : levels) {
				String code = getColumnValue(level + CODE_COLUMN_SUFFIX, true, String.class);
				String label = getColumnValue(level, true, String.class);
				line.addCodeLabelItem(code, label);
			}
			return line;
		}

	}
	
	class Validator {
		
		private static final String MISSING_REQUIRED_COLUMNS_MESSAGE_KEY = "codeListImport.parsingError.missing_required_columns.message";

		public void validate() throws ParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws ParsingException {
			List<String> colNames = getColumnNames();
			if ( colNames == null || colNames.size() % 2 != 0 ) {
				ParsingError error = new ParsingError(ErrorType.MISSING_REQUIRED_COLUMNS, 1, MISSING_REQUIRED_COLUMNS_MESSAGE_KEY);
				throw new ParsingException(error);
			}
			String levelName = null;
			for (int i = 0; i < colNames.size(); i++) {
				String colName = StringUtils.trimToEmpty(colNames.get(i));
				boolean codeColumn = i % 2 == 0;
				if ( codeColumn ) {
					//code column
					if ( colName.matches(CODE_COLUMN_EXPR) ) {
						levelName = colName.substring(0, colName.toLowerCase().lastIndexOf(CODE_COLUMN_SUFFIX.toLowerCase()));
					} else {
						String expectedColName = colName + CODE_COLUMN_SUFFIX;
						ParsingError error = new ParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName, expectedColName);
						throw new ParsingException(error);
					}
				} else if (! colName.equalsIgnoreCase(levelName) ) {
					ParsingError error = new ParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName, levelName);
					throw new ParsingException(error);
				}
			}
		}

	}

}
