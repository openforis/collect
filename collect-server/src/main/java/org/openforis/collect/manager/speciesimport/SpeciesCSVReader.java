/**
 * 
 */
package org.openforis.collect.manager.speciesimport;

import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.FORM;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SERIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.VARIETY;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.gbif.ecat.parser.UnparsableException;
import org.gbif.ecat.voc.Rank;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.CSVDataImportReader;
import org.openforis.collect.io.metadata.parsing.CSVLineParser;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.metadata.species.SpeciesFileColumn;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author S. Ricci
 *
 */
public class SpeciesCSVReader extends CSVDataImportReader<SpeciesLine> {

	private static final String HYBRID_MARKER = "×";
	
	public SpeciesCSVReader(File file, CSVFileOptions csvFileOptions) throws IOException, ParsingException {
		super(file, csvFileOptions);
	}

	@Override
	protected SpeciesCSVLineParser createLineParserInstance() {
		SpeciesCSVLineParser lineParser = SpeciesCSVLineParser.createInstance(this, currentCSVLine);
		return lineParser;
	}

	@Override
	public boolean validateAllFile() throws ParsingException {
		Validator validator = new Validator();
		validator.validate();
		return true;
	}
	
	public List<String> getLanguageColumnNames() {
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
	
	public static class SpeciesCSVLineParser extends CSVLineParser<SpeciesLine> {
		
		private static final String VERNACULAR_NAME_TRIM_EXPRESSION = "^\\s+|\\s+$|;+$|\\.+$";
		private static final String SYNONYM_COL_NAME = "synonyms";
		private static final String SYNONYM_SPLIT_EXPRESSION = "((syn|Syn)(\\.\\:|\\.|\\:|\\s))";
		private static final Pattern SYNONYM_PATTERN = Pattern.compile("^" + SYNONYM_SPLIT_EXPRESSION, Pattern.CASE_INSENSITIVE);

		private static final String DEFAULT_VERNACULAR_NAMES_SEPARATOR = ",";
		private static final String OTHER_VERNACULAR_NAMES_SEPARATOR_EXPRESSION = "/";
		
		public static final String UNEXPECTED_SYNONYM_MESSAGE_KEY = "speciesImport.parsingError.unexpected_synonym.message";

		private transient ParsedName<Object> parsedScientificName;
		private transient String rawScientificName;
		
		SpeciesCSVLineParser(SpeciesCSVReader reader, CsvLine line) {
			super(reader, line);
		}
		
		public static SpeciesCSVLineParser createInstance(SpeciesCSVReader reader, CsvLine line) {
			return new SpeciesCSVLineParser(reader, line);
		}
	
		public SpeciesLine parse() throws ParsingException {
			SpeciesLine line = super.parse();
			this.rawScientificName = extractRawScientificName();
			line.setRawScientificName(this.rawScientificName);
			this.parsedScientificName = parseRawScienfificName();
			line.setTaxonId(extractTaxonId(false));
			line.setCode(extractCode(true));
			line.setFamilyName(extractFamilyName());
			line.setGenus(extractGenus());
			line.setSpeciesName(extractSpeciesName());
			line.setRank(extractRank());
			normalizeRank(line);
			line.setCanonicalScientificName(this.rawScientificName == null ? null: extractCanonicalScientificName(line.getRank(), parsedScientificName));
			line.setLanguageToVernacularNames(extractVernacularNames());
			return line;
		}

		protected void normalizeRank(SpeciesLine line) {
			if ( line.getRank() == TaxonRank.SPECIES && line.getSpeciesName() == null ) {
				line.setRank(GENUS);
			}
		}

		protected Integer extractTaxonId(boolean required) throws ParsingException {
			return getColumnValue(SpeciesFileColumn.NO.getColumnName(), required, Integer.class);
		}

		protected String extractCode(boolean required) throws ParsingException {
			return getColumnValue(SpeciesFileColumn.CODE.getColumnName(), required, String.class);
		}
		
		protected String extractFamilyName() throws ParsingException {
			return getColumnValue(SpeciesFileColumn.FAMILY.getColumnName(), true, String.class);
		}

		protected String extractRawScientificName() throws ParsingException {
			return getColumnValue(SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName(), false, String.class);
		}
		
		protected ParsedName<Object> parseRawScienfificName() throws ParsingException {
			if ( rawScientificName == null ) {
				return null;
			} else {
				return parseRawScienfificName(rawScientificName);
			}
		}
		
		protected ParsedName<Object> parseRawScienfificName(String rawScientificName) throws ParsingException {
			try {
				//replace " x. " with " x " to avoid skipping hybrid names parsing
				String adaptedRawScientificName = rawScientificName.replaceAll(" x\\. ", " " + HYBRID_MARKER);				
				NameParser nameParser = new NameParser();
				ParsedName<Object> parsedName = nameParser.parse(adaptedRawScientificName);
				return parsedName;
			} catch (UnparsableException e) {
				ParsingError error = createFieldParsingError(
						SpeciesFileColumn.SCIENTIFIC_NAME, "scientific name", rawScientificName);
				throw new ParsingException(error);
			}
		}
		
		protected TaxonRank extractRank() throws ParsingException {
			if ( parsedScientificName == null ) {
				return FAMILY;
			} else {
				return extractRank(parsedScientificName);
			}
		}
		
		protected TaxonRank extractRank(ParsedName<Object> parsedName) throws ParsingException {
			Rank rank = parsedName.getRank();
			if ( rank == null ) {
				return GENUS;
			} else {
				switch ( rank ) {
				case FAMILY:
					return FAMILY;
				case GENUS:
					return GENUS;
				case Series:
					return SERIES;
				case SPECIES:
					return SPECIES;
				case SUBSPECIES:
					return SUBSPECIES;
				case VARIETY:
					return VARIETY;
				case Form:
					return FORM;
				default:
					return SPECIES;
				}
			}
		}
		
		protected String extractCanonicalScientificName() throws ParsingException {
			return extractCanonicalScientificName(parsedScientificName);
		}

		protected String extractCanonicalScientificName(ParsedName<Object> parsedName) throws ParsingException {
			return extractCanonicalScientificName(extractRank(parsedName), parsedName);
		}

		protected String extractCanonicalScientificName(TaxonRank rank, ParsedName<Object> parsedName) throws ParsingException {
			boolean showRankMarker;
			switch ( rank ) {
			case SPECIES:
				showRankMarker = false;
				break;
			default:
				showRankMarker = true;
			}
			//String result = parsedName.buildName(false, showRankMarker, false, false, false, true, true, false, false, false, false);
			String result = parsedName.buildName(true, showRankMarker, false, true, false, true, true, false, false, true, true);
			return result;
		}
		
		protected String extractGenus() throws ParsingException {
			String genus = parsedScientificName == null ? null: parsedScientificName.getGenusOrAbove();
			return genus;
		}
		
		protected String extractSpeciesName() throws ParsingException {
			String speciesName = parsedScientificName == null ? null: parsedScientificName.canonicalSpeciesName();
			return speciesName;
		}
		
		protected Map<String, List<String>> extractVernacularNames() throws ParsingException {
			VernacularLanguagesMap result = extractVernacularNamesFromColumns();
			if ( rawScientificName != null ) {
				List<String> synonyms = extractSynonymsFromScientificName();
				if ( ! synonyms.isEmpty() ) {
					result.addSynonyms(synonyms);
				}
			}
			return result.getMap();
		}

		private List<String> extractSynonymsFromScientificName() throws ParsingException {
			List<String> result = new ArrayList<String>();
			String[] splitted = rawScientificName.split(SYNONYM_SPLIT_EXPRESSION);
			if ( splitted != null && splitted.length > 1 ) {
				for (int i = 1; i < splitted.length; i++) {
					String synonym = splitted[i];
					ParsedName<Object> parsed = parseRawScienfificName(synonym);
					String canonicalNameSyn = extractCanonicalScientificName(parsed);
					result.add(canonicalNameSyn);
				}
			}
			return result;
		}

		protected VernacularLanguagesMap extractVernacularNamesFromColumns()
				throws ParsingException {
			VernacularLanguagesMap result = new VernacularLanguagesMap();
			List<String> languageColumnNames = ((SpeciesCSVReader) getReader()).getLanguageColumnNames();
			for (String langCode : languageColumnNames) {
				List<String> vernacularNames = extractVernacularNames(langCode);
				result.put(langCode, vernacularNames);
			}
			List<String> synonyms = extractVernacularNames(SpeciesFileColumn.SYNONYMS.getColumnName());
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

		protected ParsingError createFieldParsingError(SpeciesFileColumn column, String fieldName, String value) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, lineNumber, 
					column.getColumnName(), "Error parsing " + fieldName +" from " + value);
			return error;
		}

		protected void throwEmptyColumnParsingException(SpeciesFileColumn column)
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
			String[] requiredColumnNames = SpeciesFileColumn.REQUIRED_COLUMN_NAMES;
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
