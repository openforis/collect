/**
 * 
 */
package org.openforis.collect.io.metadata.species;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.CSVReferenceDataImportReader;
import org.openforis.collect.io.metadata.parsing.CSVReferenceDataLineParser;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author S. Ricci
 *
 */
public class SpeciesBackupCSVReader extends CSVReferenceDataImportReader<SpeciesBackupLine> {
	
	private List<String> languageColumnNames;

	public SpeciesBackupCSVReader(File file) throws IOException, ParsingException {
		super(file);
	}
	
	@Override
	public void init() throws IOException, ParsingException {
		super.init();
		this.languageColumnNames = extractLanguageColumnNames();
	}

	@Override
	protected boolean isInfoAttribute(String col) {
		Set<String> predefinedColumnNames = new HashSet<String>();
		for (SpeciesBackupFileColumn column : SpeciesBackupFileColumn.values()) {
			predefinedColumnNames.add(column.getColumnName());
		}
		return ! (predefinedColumnNames.contains(col) 
				|| Languages.getCodes(Standard.ISO_639_3).contains(col));
	}
	
	@Override
	protected SpeciesCSVLineParser createLineParserInstance() {
		SpeciesCSVLineParser lineParser = SpeciesCSVLineParser.createInstance(this, currentCSVLine, infoColumnNames);
		return lineParser;
	}

	@Override
	public boolean validateAllFile() throws ParsingException {
		Validator validator = new Validator();
		validator.validate();
		return true;
	}
	
	public List<String> extractLanguageColumnNames() {
		List<String> columnNames = getColumnNames();
		List<String> result = new ArrayList<String>();
		for (String colName : columnNames) {
			String colNameAdapted = StringUtils.trimToEmpty(colName).toLowerCase(Locale.ENGLISH);
			if ( Languages.exists(Languages.Standard.ISO_639_3, colNameAdapted) ) {
				result.add(colName);
			}
		}
		return result;
	}
	
	protected List<String> getLanguageColumnNames() {
		return languageColumnNames;
	}
	
	public static class SpeciesCSVLineParser extends CSVReferenceDataLineParser<SpeciesBackupLine> {
		
		private static final String VERNACULAR_NAME_TRIM_EXPRESSION = "^\\s+|\\s+$|;+$|\\.+$";
		private static final String SYNONYM_COL_NAME = "synonyms";
		private static final String SYNONYM_SPLIT_EXPRESSION = "((syn|Syn)(\\.\\:|\\.|\\:|\\s))";
		private static final Pattern SYNONYM_PATTERN = Pattern.compile("^" + SYNONYM_SPLIT_EXPRESSION, Pattern.CASE_INSENSITIVE);

		private static final String DEFAULT_VERNACULAR_NAMES_SEPARATOR = ",";
		private static final String OTHER_VERNACULAR_NAMES_SEPARATOR_EXPRESSION = "/";
		
		public static final String UNEXPECTED_SYNONYM_MESSAGE_KEY = "speciesImport.parsingError.unexpected_synonym.message";

		SpeciesCSVLineParser(SpeciesBackupCSVReader reader, CsvLine line, List<String> infoColumnNames) {
			super(reader, line, infoColumnNames);
		}
		
		public static SpeciesCSVLineParser createInstance(SpeciesBackupCSVReader reader, CsvLine line, List<String> infoColumnNames) {
			return new SpeciesCSVLineParser(reader, line, infoColumnNames);
		}
	
		public SpeciesBackupLine parse() throws ParsingException {
			SpeciesBackupLine line = super.parse();
			line.setId(extractId(true));
			line.setParentId(extractParentId(false));
			line.setRank(extractRank());
			line.setNo(extractTaxonNo(false));
			line.setCode(extractCode(false));
			line.setScientificName(extractScientificName());
			line.setLanguageToVernacularNames(extractVernacularNamesFromColumns().getMap());
			return line;
		}

		protected Long extractId(boolean required) throws ParsingException {
			return getColumnValue(SpeciesBackupFileColumn.ID.getColumnName(), required, Long.class);
		}

		protected Long extractParentId(boolean required) throws ParsingException {
			return getColumnValue(SpeciesBackupFileColumn.PARENT_ID.getColumnName(), required, Long.class);
		}

		private TaxonRank extractRank() throws ParsingException {
			String taxonName = getColumnValue(SpeciesBackupFileColumn.RANK.getColumnName(), true, String.class);
			TaxonRank taxon = TaxonRank.fromName(taxonName);
			return taxon;
		}

		protected Integer extractTaxonNo(boolean required) throws ParsingException {
			return getColumnValue(SpeciesBackupFileColumn.NO.getColumnName(), required, Integer.class);
		}
		
		protected String extractCode(boolean required) throws ParsingException {
			return getColumnValue(SpeciesBackupFileColumn.CODE.getColumnName(), required, String.class);
		}
		
		protected String extractScientificName() throws ParsingException {
			return getColumnValue(SpeciesBackupFileColumn.SCIENTIFIC_NAME.getColumnName(), false, String.class);
		}
		
		protected VernacularLanguagesMap extractVernacularNamesFromColumns()
				throws ParsingException {
			VernacularLanguagesMap result = new VernacularLanguagesMap();
			List<String> languageColumnNames = ((SpeciesBackupCSVReader) getReader()).getLanguageColumnNames();
			for (String langCode : languageColumnNames) {
				List<String> vernacularNames = extractVernacularNames(langCode);
				result.put(langCode, vernacularNames);
			}
			List<String> synonyms = extractVernacularNames(SpeciesBackupFileColumn.SYNONYMS.getColumnName());
			result.addSynonyms(synonyms);
			return result;
		}

		protected List<String> extractVernacularNames(String colName) throws ParsingException {
			String colValue = StringUtils.normalizeSpace(getColumnValue(colName, false, String.class));
			if ( StringUtils.isBlank(colValue) ) {
				return new ArrayList<String>();
			} else {
				List<String> result = new ArrayList<String>();
				String normalized = colValue.replaceAll(OTHER_VERNACULAR_NAMES_SEPARATOR_EXPRESSION, DEFAULT_VERNACULAR_NAMES_SEPARATOR);
				String[] split = StringUtils.split(normalized, DEFAULT_VERNACULAR_NAMES_SEPARATOR);
				for (String splitPart : split) {
					String trimmedPart = extractVernacularName(colName, splitPart);
					if ( trimmedPart != null ) {
						result.add(trimmedPart);
					}
				}
				return result;
			}
		}

		private String extractVernacularName(String colName, String splitPart) throws ParsingException {
			String trimmed = splitPart.replaceAll(VERNACULAR_NAME_TRIM_EXPRESSION, "");
			if ( trimmed.length() > 0 ) {
				Matcher matcher = SYNONYM_PATTERN.matcher(trimmed);
				if ( matcher.find() ) {
					if ( SYNONYM_COL_NAME.equals(colName) ) {
						matcher.replaceAll("");
					} else {
						ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, lineNumber, colName, UNEXPECTED_SYNONYM_MESSAGE_KEY);
						throw new ParsingException(error);
					}
				}
				return trimmed;
			} else {
				return null;
			}
		}

		protected ParsingError createFieldParsingError(SpeciesBackupFileColumn column, String fieldName, String value) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, lineNumber, 
					column.getColumnName(), "Error parsing " + fieldName +" from " + value);
			return error;
		}

		protected void throwEmptyColumnParsingException(SpeciesBackupFileColumn column)
				throws ParsingException {
			ParsingError error = new ParsingError(ErrorType.EMPTY, lineNumber, column.getColumnName());
			throw new ParsingException(error);
		}

	}
	
	class Validator {
		
		public void validate() throws ParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws ParsingException {
			List<String> colNames = getColumnNames();
			String[] requiredColumnNames = SpeciesBackupFileColumn.REQUIRED_COLUMN_NAMES;
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
	
	static class VernacularLanguagesMap {
		private static final String LATIN_LANGUAGE_CODE = "lat";
		
		private Map<String, List<String>> langCodeToVernacularNames;
		
		public VernacularLanguagesMap() {
			langCodeToVernacularNames = new HashMap<String, List<String>>();
		}

		public Map<String, List<String>> getMap() {
			return langCodeToVernacularNames;
		}

		public void put(String langCode, List<String> vernacularNames) {
			langCodeToVernacularNames.put(langCode, vernacularNames);
		}

		public void addSynonyms(List<String> synonyms) {
			List<String> oldSynonyms = langCodeToVernacularNames.get(LATIN_LANGUAGE_CODE);
			if ( oldSynonyms == null ) {
				langCodeToVernacularNames.put(LATIN_LANGUAGE_CODE, synonyms);
			} else {
				oldSynonyms.addAll(synonyms);
			}
		}
		
	}

}
