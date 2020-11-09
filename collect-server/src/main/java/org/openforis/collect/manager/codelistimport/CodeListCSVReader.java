/**
 * 
 */
package org.openforis.collect.manager.codelistimport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public static final String QUALIFIABLE_COLUMN_SUFFIX = "_qualifiable";

	private ColumnsInfo columnsInfo;
//	private List<String> levels;
	private List<String> languages;
	private String defaultLanguage;
	
	public CodeListCSVReader(File file, CSVFileOptions csvFileOptions, List<String> languages, String defaultLanguage) throws IOException, ParsingException {
		super(file, csvFileOptions);
		this.languages = languages;
		this.defaultLanguage = defaultLanguage;
	}
	
	@Override
	protected void readHeaders() throws IOException {
		super.readHeaders();
		columnsInfo = new ColumnsInfoExtractor().extractColumnsInfo(csvReader.getColumnNames(), defaultLanguage);
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
	
	public List<String> getLevels() {
		return getColumnsInfo().getLevelNames();
	}
	
	public ColumnsInfo getColumnsInfo() {
		return columnsInfo;
	}
	
	public List<String> getLanguages() {
		return languages;
	}
	
	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public static class CodeListCSVLineParser extends CSVLineParser<CodeListLine> {
		
		private static final int MAX_LENGTH_CODE = 255;
		private static final int MAX_LENGTH_LABEL = 255;
		private static final int MAX_LENGTH_DESCRIPTION = 1023;

		CodeListCSVLineParser(CodeListCSVReader reader, CsvLine line) {
			super(reader, line);
		}
		
		public static CodeListCSVLineParser createInstance(CodeListCSVReader reader, CsvLine line) {
			return new CodeListCSVLineParser(reader, line);
		}
	
		public CodeListLine parse() throws ParsingException {
			CodeListLine line = super.parse();
			ColumnsInfo columnsInfo = ((CodeListCSVReader) reader).getColumnsInfo();
			List<String> levels = columnsInfo.getLevelNames();
			for (int levelIdx = 0; levelIdx < levels.size(); levelIdx++) {
				String level = levels.get(levelIdx);
				String codeColumnName = columnsInfo.getCodeColumnInfo(level).getColumnName();
				String code = getColumnValue(codeColumnName, String.class, false, MAX_LENGTH_CODE);
				if ( code != null ) {
					line.addLevelCode(code);
					// Labels
					for (ColumnInfo columnInfo : columnsInfo.getLabelColumnInfos(levelIdx)) {
						String colValue = getColumnValue(columnInfo.getColumnName(), String.class, false, MAX_LENGTH_LABEL);
						if ( colValue != null ) {
							line.addLabel(levelIdx, columnInfo.getLangCode(), colValue);
						}
					}
					// Descriptions
					for (ColumnInfo columnInfo : columnsInfo.getDescriptionColumnInfos(levelIdx)) {
						String colValue = getColumnValue(columnInfo.getColumnName(), String.class, false, MAX_LENGTH_DESCRIPTION);
						if ( colValue != null ) {
							line.addDescription(levelIdx, columnInfo.getLangCode(), colValue);
						}
					}
					// Qualifiable
					ColumnInfo qualifiableColumnInfo = columnsInfo.getQualifiableColumnInfo(levelIdx);
					if (qualifiableColumnInfo != null) {
						String colValue = getColumnValue(qualifiableColumnInfo.getColumnName(), false, String.class);
						boolean qualifiable = colValue != null && ("true".equalsIgnoreCase(colValue) || "1".equals(colValue));
						line.setQualifiable(levelIdx, qualifiable);
					}
				} else {
					break;
				}
			}
			return line;
		}

	}
	
	enum ColumnType {
		CODE,
		LABEL,
		DESCRIPTION,
		QUALIFIABLE,
		INVALID,
	}
	
	public static class ColumnInfo {
		private String columnName;
		ColumnType type;
		String levelName;
		Integer levelIndex;
		String langCode;

		public ColumnInfo(String columnName, ColumnType type) {
			this(columnName, type, null, null);
		}
		
		public ColumnInfo(String columnName, ColumnType type, String levelName, Integer levelIndex) {
			this(columnName, type, levelName, levelIndex, null);
		}
		
		public ColumnInfo(String columnName, ColumnType type, String levelName, Integer levelIndex, String langCode) {
			super();
			this.columnName = columnName;
			this.type = type;
			this.levelName = levelName;
			this.levelIndex = levelIndex;
			this.langCode = langCode;
		}
		
		public String getColumnName() {
			return columnName;
		}
		
		public ColumnType getType() {
			return type;
		}
		
		public String getLevelName() {
			return levelName;
		}
		
		public Integer getLevelIndex() {
			return levelIndex;
		}
		
		public String getLangCode() {
			return langCode;
		}
		
		public void setLangCode(String langCode) {
			this.langCode = langCode;
		}
	}
	
	public static class ColumnsInfo {
		
		private Map<String, ColumnInfo> infoByColumnName = new HashMap<String, ColumnInfo>();
		private List<String> levelNames = new ArrayList<String>();
		
		public void addColumnInfo(ColumnInfo columnInfo) {
			infoByColumnName.put(columnInfo.getColumnName(), columnInfo);
			if (columnInfo.getType() == ColumnType.CODE) {
				levelNames.add(columnInfo.getLevelName());
			}
		}
		
		public ColumnInfo getQualifiableColumnInfo(int levelIdx) {
			List<ColumnInfo> columnsInfo = getColumnInfosByType(levelIdx, ColumnType.QUALIFIABLE);
			return columnsInfo.isEmpty() ? null : columnsInfo.get(0);
		}

		private List<ColumnInfo> getColumnInfosByType(int levelIdx, ColumnType type) {
			List<ColumnInfo> result = new ArrayList<ColumnInfo>();
			Collection<ColumnInfo> values = infoByColumnName.values();
			for (ColumnInfo columnInfo : values) {
				if (columnInfo.getLevelIndex() == levelIdx && columnInfo.getType() == type) {
					result.add(columnInfo);
				}
			}
			return result;
		}
		
		public List<ColumnInfo> getLabelColumnInfos(int levelIdx) {
			return getColumnInfosByType(levelIdx, ColumnType.LABEL);
		}
		
		public List<ColumnInfo> getDescriptionColumnInfos(int levelIdx) {
			return getColumnInfosByType(levelIdx, ColumnType.DESCRIPTION);
		}
		
		public ColumnInfo getInfo(String colName) {
			return infoByColumnName.get(colName);
		}
		
		public ColumnInfo getCodeColumnInfo(String levelName) {
			return infoByColumnName.get(levelName + CODE_COLUMN_SUFFIX);
		}
		
		public List<String> getLevelNames() {
			return levelNames;
		}
	}
	
	static class ColumnsInfoExtractor {
		private static final String LEVEL_NAME_COLUMN_EXPR = "[a-z]\\w*";
		private static final Pattern CODE_COLUMN_PATTERN = toPattern("(" + LEVEL_NAME_COLUMN_EXPR + ")" + CODE_COLUMN_SUFFIX);
		/**
		 * Label column regular expression 1: only LEVEL_NAME
		 */
		private static final String LABEL_COLUMN_EXPR = "(" + LEVEL_NAME_COLUMN_EXPR + ")";
		private static final Pattern LABEL_COLUMN_PATTERN = toPattern(LABEL_COLUMN_EXPR);
		private static final String LANG_OPTIONAL_EXPR = "(_([a-z]{2}))?";
		/**
		 * Label column regular expression 2: LEVEL_NAME_label and (optional) _LANG_CODE
		 */
		private static final String LABEL_COLUMN_WITH_LANG_CODE_EXPR = LABEL_COLUMN_EXPR + LABEL_COLUMN_SUFFIX + LANG_OPTIONAL_EXPR;
		private static final Pattern LABEL_COLUMN_WITH_LANG_CODE_PATTERN = toPattern(LABEL_COLUMN_WITH_LANG_CODE_EXPR);

		private static final Pattern DESCRIPTION_COLUMN_PATTERN = toPattern("(" + LEVEL_NAME_COLUMN_EXPR + ")" +  DESCRIPTION_COLUMN_SUFFIX + LANG_OPTIONAL_EXPR);
		private static final Pattern QUALIFIABLE_COLUMN_PATTERN = toPattern("(" + LEVEL_NAME_COLUMN_EXPR + ")" +  QUALIFIABLE_COLUMN_SUFFIX);

		private static final Pattern toPattern(String expr) {
			return Pattern.compile("^" + expr + "$");
		}
		
		public ColumnsInfo extractColumnsInfo(List<String> colNames, String defaultLanguage) {
			ColumnsInfo columnsInfo = new ColumnsInfo();
			int prevLevelIndex = -1;
			String prevLevelName = null;
			for (String colName : colNames) {
				ColumnInfo columnInfo = extractColumnInfo(colName, prevLevelIndex, prevLevelName, defaultLanguage);
				if (columnInfo.getType() == ColumnType.CODE) {
					prevLevelIndex ++;
					prevLevelName = columnInfo.getLevelName();
				}
				columnsInfo.addColumnInfo(columnInfo);
			}
			return columnsInfo;
		}
	
		private ColumnInfo extractColumnInfo(String colName, int levelIndex, String expectedLevelName, String defaultLanguage) {
			try {
				// CODE
				{
					Matcher matcher = CODE_COLUMN_PATTERN.matcher(colName);
					if ( matcher.matches() ) {
						String levelName = matcher.group(1);
						return new ColumnInfo(colName, ColumnType.CODE, levelName, levelIndex + 1);
					}
				}
				// DESCRIPTION
				{
					Matcher matcher = DESCRIPTION_COLUMN_PATTERN.matcher(colName);
					if (matcher.matches()) {
						String levelName = matcher.group(1);
						checkLevelName(expectedLevelName, levelName);
						String langCode = matcher.groupCount() > 2 && matcher.group(3) != null 
								? matcher.group(3) 
								: defaultLanguage;
						return new ColumnInfo(colName, ColumnType.DESCRIPTION, levelName, levelIndex, langCode);
					}
				}
				// QUALIFIABLE
				{
					Matcher matcher = QUALIFIABLE_COLUMN_PATTERN.matcher(colName);
					if (matcher.matches()) {
						String levelName = matcher.group(1);
						checkLevelName(expectedLevelName, levelName);
						return new ColumnInfo(colName, ColumnType.QUALIFIABLE, levelName, levelIndex);
					}
				}
				// LABEL
				{
					// Try to check if there is a lang code (optional)
					Matcher matcher = LABEL_COLUMN_WITH_LANG_CODE_PATTERN.matcher(colName);
					if (!matcher.matches()) {
						// Try to check if the column ends with _label or if it's only equal to the level name
						matcher = LABEL_COLUMN_PATTERN.matcher(colName);
					}
					if (matcher.matches()) {
						String levelName = matcher.group(1);
						checkLevelName(expectedLevelName, levelName);
						ColumnInfo columnInfo = new ColumnInfo(colName, ColumnType.LABEL, levelName, levelIndex);
						String langCode = matcher.groupCount() > 2 && matcher.group(3) != null
								? matcher.group(3) 
								: defaultLanguage;
						columnInfo.setLangCode(langCode);
						return columnInfo;
					}
				}
			} catch (InvalidLevelNameException e) {
				return new ColumnInfo(colName, ColumnType.INVALID, e.getLevelName(), levelIndex);
			}
			return new ColumnInfo(colName, ColumnType.INVALID);
		}
		
		private void checkLevelName(String expectedLevelName, String levelName) throws InvalidLevelNameException {
			if (expectedLevelName == null || !expectedLevelName.equals(levelName)) {
				throw new InvalidLevelNameException(expectedLevelName, levelName);
			}
		}
		
		static class InvalidLevelNameException extends Exception {
			private static final long serialVersionUID = 1L;
			
			private String levelName;

			public InvalidLevelNameException(String expectedLevelName, String levelName) {
				super(String.format("Invalid level name: expected %s found %s", expectedLevelName, levelName));
				this.levelName = levelName;
			}
			
			private String getLevelName() {
				return levelName;
			}
		}
	}
	
	class Validator {
		
		private static final String MISSING_REQUIRED_COLUMNS_MESSAGE_KEY = "codeListImport.parsingError.missing_required_columns.message";
		private static final String LANGUAGE_CODE_NOT_DEFINED_MESSAGE_KEY = "codeListImport.parsingError.language_code_not_defined";


		public void validate() throws ParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws ParsingException {
			//at least one code column is required
			if (columnsInfo.getLevelNames().isEmpty()) {
				throw new ParsingException(new ParsingError(ErrorType.MISSING_REQUIRED_COLUMNS, 1, 
						(String) null, MISSING_REQUIRED_COLUMNS_MESSAGE_KEY));
			} else {
				for (String colName : getColumnNames()) {
					ColumnInfo columnInfo = columnsInfo.getInfo(colName);
					switch (columnInfo.getType()) {
					case INVALID:
						throw new ParsingException(new ParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName));
					case LABEL:
					case DESCRIPTION:
						if (!languages.contains(columnInfo.getLangCode())) {
							throw new ParsingException(new ParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName, 
								LANGUAGE_CODE_NOT_DEFINED_MESSAGE_KEY));
						}
						break;
					default:
					}
				}
			}
		}

	}
	
}
