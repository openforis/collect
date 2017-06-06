/**
 * 
 */
package org.openforis.collect.manager.codelistimport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.CSVDataImportReader;
import org.openforis.collect.io.metadata.parsing.CSVLineParser;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.openforis.commons.io.csv.CsvLine;

/**
 * @author S. Ricci
 *
 */
public class CodeListCSVReader extends CSVDataImportReader<CodeListLine> {

	public static final String FLAT_LIST_CODE_COLUMN_NAME = "code";
	public static final String FLAT_LIST_LABEL_COLUMN_NAME = "label";
	public static final String CODE_COLUMN_SUFFIX = "_code";
	public static final String LABEL_COLUMN_SUFFIX = "_label";
	public static final String DESCRIPTION_COLUMN_SUFFIX = "_description";
	public static final String LEVEL_NAME_COLUMN_EXPR = "[a-z][a-z0-9_]*";
	public static final String CODE_COLUMN_EXPR = "^" + LEVEL_NAME_COLUMN_EXPR + CODE_COLUMN_SUFFIX + "$";
	public static final String LABEL_COLUMN_EXPR = "^" + LEVEL_NAME_COLUMN_EXPR + "$";

	private List<String> levels;
	private List<String> languages;
	private String defaultLanguage;
	
	public CodeListCSVReader(File file, CSVFileOptions csvFileOptions, List<String> languages, String defaultLanguage) throws IOException, ParsingException {
		super(file, csvFileOptions);
		this.languages = languages;
		this.defaultLanguage = defaultLanguage;
	}
	
	@Override
	public void init() throws IOException, ParsingException {
		super.init();
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
				String levelName = extractLevelName(colName);
				levels.add(levelName);
			}
		}
	}

	private String extractLevelName(String colName) {
		String levelName = colName.substring(0, colName.lastIndexOf(CODE_COLUMN_SUFFIX));
		return levelName;
	}
	
	private static String[] getPossibleDefaultLanguageLabelColumnNames(String defaultLanguage, String level) {
		String[] colNames = {level, level + LABEL_COLUMN_SUFFIX, level + LABEL_COLUMN_SUFFIX + "_" + defaultLanguage};
		return colNames;
	}

	private static String[] getPossibleDefaultLanguageDescriptionColumnNames(String defaultLanguage, String level) {
		String[] colNames = {level, level + DESCRIPTION_COLUMN_SUFFIX, level + DESCRIPTION_COLUMN_SUFFIX + "_" + defaultLanguage};
		return colNames;
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
					addDescriptions(line, languages, level, i);
				} else {
					break;
				}
			}
			return line;
		}

		private void addLabels(CodeListLine line, List<String> languages,
				String level, int levelIdx) throws ParsingException {
			//add default language label
			String defaultLangLabel = getDefaultLanguageLabel(level);
			if ( defaultLangLabel != null ) {
				String defaultLang = ((CodeListCSVReader) reader).getDefaultLanguage();
				line.addLabel(levelIdx, defaultLang, defaultLangLabel);
			}
			//add labels per each language
			for (String lang : languages) {
				String textColumnName = level + LABEL_COLUMN_SUFFIX + "_" + lang;
				String l = getColumnValue(textColumnName, false, String.class);
				if ( l != null ) {
					line.addLabel(levelIdx, lang, l);
				}
			}
		}

		private void addDescriptions(CodeListLine line, List<String> languages,
				String level, int levelIdx) throws ParsingException {
			//add default language description
			String defaultLangLabel = getDefaultLanguageDescription(level);
			if ( defaultLangLabel != null ) {
				String defaultLang = ((CodeListCSVReader) reader).getDefaultLanguage();
				line.addDescription(levelIdx, defaultLang, defaultLangLabel);
			}
			//add labels per each language
			for (String lang : languages) {
				String textColumnName = level + DESCRIPTION_COLUMN_SUFFIX + "_" + lang;
				String l = getColumnValue(textColumnName, false, String.class);
				if ( l != null ) {
					line.addDescription(levelIdx, lang, l);
				}
			}
		}

		private String getDefaultLanguageLabel(String levelName)
				throws ParsingException {
			String defaultLangLabel = null;
			String defaultLang = ((CodeListCSVReader) reader).getDefaultLanguage();
			String[] defaultLangColNames = getPossibleDefaultLanguageLabelColumnNames(defaultLang, levelName);
			for (String defaultLangColName : defaultLangColNames) {
				String label = getColumnValue(defaultLangColName, false, String.class);
				if ( label != null ) {
					defaultLangLabel = label;
					break;
				}
			}
			return defaultLangLabel;
		}

		private String getDefaultLanguageDescription(String levelName)
				throws ParsingException {
			String defaultLangLabel = null;
			String defaultLang = ((CodeListCSVReader) reader).getDefaultLanguage();
			String[] defaultLangColNames = getPossibleDefaultLanguageDescriptionColumnNames(defaultLang, levelName);
			for (String defaultLangColName : defaultLangColNames) {
				String label = getColumnValue(defaultLangColName, false, String.class);
				if ( label != null ) {
					defaultLangLabel = label;
					break;
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
			//at least one code column is required
			List<String> colNames = getColumnNames();
			Collection<String> levelNames = new HashSet<String>();
			for (int i = 0; i < colNames.size(); i++) {
				String colName = StringUtils.trimToEmpty(colNames.get(i));
				if ( colName.matches(CODE_COLUMN_EXPR) ) {
					String levelName = extractLevelName(colName);
					levelNames.add(levelName);
				}
			}
			//a default language label column for each level is required
			boolean defaultLabelColumnsFound = true;
			String defaultLang = getDefaultLanguage();
			for (String levelName : levelNames) {
				boolean columnFound = false;
				String[] defaultLanguageLabelColumnNames = getPossibleDefaultLanguageLabelColumnNames(defaultLang, levelName);
				for (String colName : defaultLanguageLabelColumnNames) {
					if ( colNames.contains(colName) ) {
						columnFound = true;
						break;
					}
				}
				if ( ! columnFound ) {
					defaultLabelColumnsFound = false;
					break;
				}
			}
			if ( levelNames.isEmpty() || ! defaultLabelColumnsFound ) {
				ParsingError error = new ParsingError(ErrorType.MISSING_REQUIRED_COLUMNS, 1, 
						(String) null, MISSING_REQUIRED_COLUMNS_MESSAGE_KEY);
				throw new ParsingException(error);
			}
		}

	}

}
