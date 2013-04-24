/**
 * 
 */
package org.openforis.collect.manager.speciesimport;

import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.VARIETY;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.gbif.ecat.parser.UnparsableException;
import org.gbif.ecat.voc.Rank;
import org.openforis.collect.manager.referencedataimport.CSVDataImportReader;
import org.openforis.collect.manager.referencedataimport.CSVLineParser;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.referencedataimport.ParsingException;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author S. Ricci
 *
 */
public class SpeciesCSVReader extends CSVDataImportReader<SpeciesLine> {

	public SpeciesCSVReader(String filename) throws IOException, ParsingException {
		super(filename);
	}

	public SpeciesCSVReader(Reader reader) throws IOException, ParsingException {
		super(reader);
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
			String colNameAdapted = StringUtils.trimToEmpty(colName).toLowerCase();
			if ( Languages.exists(Languages.Standard.ISO_639_3, colNameAdapted) ) {
				result.add(colName);
			}
		}
		return result;
	}
	
	public static class SpeciesCSVLineParser extends CSVLineParser<SpeciesLine> {
		
		private static final String VERNACULAR_NAME_TRIM_EXPRESSION = "^\\s+|\\s+$|;+$|\\.+$";
		private static final String SYNONYM_COL_NAME = "";
		private static final String SYNONYM_SPLIT_EXPRESSION = "((syn|Syn)(\\.\\:|\\.|\\:|\\s))";
		private static final Pattern SYNONYM_PATTERN = Pattern.compile("^" + SYNONYM_SPLIT_EXPRESSION, Pattern.CASE_INSENSITIVE);

		private static final String DEFAULT_VERNACULAR_NAMES_SEPARATOR = ",";
		private static final String OTHER_VERNACULAR_NAMES_SEPARATOR_EXPRESSION = "/";
		
		public static final String UNEXPECTED_SYNONYM_MESSAGE_KEY = "speciesImport.parsingError.unexpected_synonym.message";

		private ParsedName<Object> parsedScientificName;
		private String rawScientificName;
		
		SpeciesCSVLineParser(SpeciesCSVReader reader, CsvLine line) {
			super(reader, line);
		}
		
		public static SpeciesCSVLineParser createInstance(SpeciesCSVReader reader, CsvLine line) {
			return new SpeciesCSVLineParser(reader, line);
		}
	
		public SpeciesLine parse() throws ParsingException {
			SpeciesLine line = super.parse();
			this.rawScientificName = extractRawScientificName();
			this.parsedScientificName = parseRawScienfificName();
			line.setTaxonId(extractTaxonId(false));
			line.setCode(extractCode(true));
			line.setFamilyName(extractFamilyName());
			line.setRawScientificName(this.rawScientificName);
			line.setGenus(extractGenus());
			line.setSpeciesName(extractSpeciesName());
			line.setRank(extractRank());
			normalizeRank(line);
			line.setCanonicalScientificName(extractCanonicalScientificName(line.getRank(), parsedScientificName));
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
			return getColumnValue(SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName(), true, String.class);
		}
		
		protected ParsedName<Object> parseRawScienfificName() throws ParsingException {
			return parseRawScienfificName(rawScientificName);
		}
		
		protected ParsedName<Object> parseRawScienfificName(String rawScientificName) throws ParsingException {
			try {
				NameParser nameParser = new NameParser();
				ParsedName<Object> parsedName = nameParser.parse(rawScientificName);
				return parsedName;
			} catch (UnparsableException e) {
				ParsingError error = createFieldParsingError(
						SpeciesFileColumn.SCIENTIFIC_NAME, "scientific name", rawScientificName);
				throw new ParsingException(error);
			}
		}
		
		protected TaxonRank extractRank() throws ParsingException {
			return extractRank(parsedScientificName);
		}
		
		protected TaxonRank extractRank(ParsedName<Object> parsedName) throws ParsingException {
			Rank rank = parsedName.getRank();
			TaxonRank taxonRank;
			if ( rank == null ) {
				taxonRank = GENUS;
			} else {
				switch ( rank ) {
				case FAMILY:
					taxonRank = FAMILY;
					break;
				case GENUS:
					taxonRank = GENUS;
					break;
				case SPECIES:
					taxonRank = SPECIES;
					break;
				case SUBSPECIES:
					taxonRank = SUBSPECIES;
					break;
				case VARIETY:
					taxonRank = VARIETY;
					break;
				default:
					taxonRank = SPECIES;
				}
			}
			return taxonRank;
		}
		
		protected String extractCanonicalScientificName() throws ParsingException {
			return extractCanonicalScientificName(parsedScientificName);
		}

		protected String extractCanonicalScientificName(ParsedName<Object> parsedName) throws ParsingException {
			return extractCanonicalScientificName(extractRank(parsedName), parsedName);
		}

		protected String extractCanonicalScientificName(TaxonRank rank, ParsedName<Object> parsedName) throws ParsingException {
			boolean showRankMarker = rank == GENUS || rank == SUBSPECIES || rank == VARIETY;
			String result = parsedName.buildName(false, showRankMarker, false, false, false, true, true, false, false, false, false);
			return result;
		}
		
		protected String extractGenus() throws ParsingException {
			String genus = parsedScientificName.getGenusOrAbove();
			return genus;
		}
		
		protected String extractSpeciesName() throws ParsingException {
			String speciesName = parsedScientificName.canonicalSpeciesName();
			return speciesName;
		}
		
		protected Map<String, List<String>> extractVernacularNames() throws ParsingException {
			VernacularLanguagesMap result = extractVernacularNamesFromColumns();
			List<String> synonyms = extractSynonymsFromScientificName();
			if ( ! synonyms.isEmpty() ) {
				result.addSynonyms(synonyms);
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
