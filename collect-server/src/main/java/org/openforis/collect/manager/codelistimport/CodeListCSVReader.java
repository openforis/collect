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
	public static final String LABEL_COLUMN_SUFFIX = "_label";
	public static final String LEVEL_NAME_COLUMN_EXPR = "[a-z][a-z0-9_]*";
	public static final String CODE_COLUMN_EXPR = "^" + LEVEL_NAME_COLUMN_EXPR + CODE_COLUMN_SUFFIX + "$";
	public static final String LABEL_COLUMN_EXPR = "^" + LEVEL_NAME_COLUMN_EXPR + "$";

	private List<String> levels;
	private List<String> languages;
	private String defaultLanguage;
	
	public CodeListCSVReader(String filename, List<String> languages, String defaultLanguage) throws IOException, ParsingException {
		super(filename);
		this.languages = languages;
		this.defaultLanguage = defaultLanguage;
		initLevels();
	}

	public CodeListCSVReader(Reader reader, List<String> languages, String defaultLanguage) throws IOException, ParsingException {
		super(reader);
		this.languages = languages;
		this.defaultLanguage = defaultLanguage;
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
			if ( colName.matches(CODE_COLUMN_EXPR) ) {
				String levelName = colName.substring(0, colName.lastIndexOf(CODE_COLUMN_SUFFIX));
				levels.add(levelName);
			}
		}
	}
	
	public List<String> getLevels() {
		return levels;
	}
	
	public List<String> getLanguages() {
		return languages;
	}
	
	public String getDefaultLanguage() {
		return defaultLanguage;
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
			List<String> languages = ((CodeListCSVReader) reader).getLanguages();
			List<String> levels = ((CodeListCSVReader) reader).getLevels();
			for (int i = 0; i < levels.size(); i++) {
				String level = levels.get(i);
				String codeColumnName = level + CODE_COLUMN_SUFFIX;
				String code = getColumnValue(codeColumnName, false, String.class);
				if ( code != null ) {
					line.addLevelCode(code);
					addLabels(line, languages, level, i);
				} else {
					break;
				}
			}
			return line;
		}

		private void addLabels(CodeListLine line, List<String> languages,
				String level, int levelIdx) throws ParsingException {
			//add default language label
			String defaultLangLabel = getLevelDefaultLanguageLabel(level);
			if ( defaultLangLabel != null ) {
				String defaultLang = ((CodeListCSVReader) reader).getDefaultLanguage();
				line.addLabel(levelIdx, defaultLang, defaultLangLabel);
			}
			//add labels per each language
			for (String lang : languages) {
				String labelColumnName = level + LABEL_COLUMN_SUFFIX + "_" + lang;
				String l = getColumnValue(labelColumnName, false, String.class);
				if ( l != null ) {
					line.addLabel(levelIdx, lang, l);
				}
			}
		}

		private String getLevelDefaultLanguageLabel(String level)
				throws ParsingException {
			String defaultLangLabel = null;
			String[] defaultLangLabelColNames = {level, level + LABEL_COLUMN_SUFFIX};
			for (String defaultLangLabelColName : defaultLangLabelColNames) {
				String label = getColumnValue(defaultLangLabelColName, false, String.class);
				if ( label != null ) {
					defaultLangLabel = label;
				}
			}
			return defaultLangLabel;
		}
		
	}
	
	class Validator {
		
		private static final String MISSING_REQUIRED_COLUMNS_MESSAGE_KEY = "codeListImport.parsingError.missing_required_columns.message";

		public void validate() throws ParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws ParsingException {
			List<String> colNames = getColumnNames();
			boolean codeColumnFound = false;
			for (int i = 0; i < colNames.size(); i++) {
				String colName = StringUtils.trimToEmpty(colNames.get(i));
				if ( colName.matches(CODE_COLUMN_EXPR) ) {
					codeColumnFound = true;
					break;
				}
			}
			if ( ! codeColumnFound ) {
				ParsingError error = new ParsingError(ErrorType.MISSING_REQUIRED_COLUMNS, 1, 
						(String) null, MISSING_REQUIRED_COLUMNS_MESSAGE_KEY);
				throw new ParsingException(error);
			}
		}

	}

}
