/**
 * 
 */
package org.openforis.collect.manager.speciesImport;

import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
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
import org.openforis.collect.manager.referenceDataImport.CSVDataImportReader;
import org.openforis.collect.manager.referenceDataImport.CSVLineParser;
import org.openforis.collect.manager.referenceDataImport.ParsingError;
import org.openforis.collect.manager.referenceDataImport.ParsingError.ErrorType;
import org.openforis.collect.manager.referenceDataImport.ParsingException;
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
		int fixedColumnsLength = SpeciesFileColumn.values().length;
		if ( columnNames.size() > fixedColumnsLength ) {
			return columnNames.subList(fixedColumnsLength, columnNames.size());
		} else {
			return Collections.emptyList();
		}
	}
	
	public static class SpeciesCSVLineParser extends CSVLineParser<SpeciesLine> {
		
		private static final String LATIN_LANGUAGE_CODE = "lat";
		private static final String VERNACULAR_NAME_TRIM_EXPRESSION = "^\\s+|\\s+$|;+$|\\.+$";
		private static final String SYNONYM_COL_NAME = "";
		private static final String SYNONYM_SPLIT_EXPRESSION = "((syn|Syn)(\\.\\:|\\.|\\:|\\s))";
		private static final Pattern SYNONYM_PATTERN = Pattern.compile("^" + SYNONYM_SPLIT_EXPRESSION, Pattern.CASE_INSENSITIVE);
		
		private static final String DEFAULT_VERNACULAR_NAMES_SEPARATOR = ",";
		private static final String OTHER_VERNACULAR_NAMES_SEPARATOR_EXPRESSION = "/";

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
			line.setCode(extractCode(false));
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
			return getColumnValue(SpeciesFileColumn.NO.getName(), required, Integer.class);
		}

		protected String extractCode(boolean required) throws ParsingException {
			return getColumnValue(SpeciesFileColumn.CODE.getName(), required, String.class);
		}
		
		protected String extractFamilyName() throws ParsingException {
			return getColumnValue(SpeciesFileColumn.FAMILY.getName(), true, String.class);
		}

		protected String extractRawScientificName() throws ParsingException {
			return getColumnValue(SpeciesFileColumn.SCIENTIFIC_NAME.getName(), true, String.class);
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
				case VARIETY:
					taxonRank = SUBSPECIES;
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
			boolean showRankMarker = rank == GENUS || rank == SUBSPECIES;
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
			Map<String, List<String>> result = extractVernacularNamesFromColumns();
			List<String> synonyms = extractSynonyms();
			if ( ! synonyms.isEmpty() ) {
				List<String> oldSynonyms = result.get(LATIN_LANGUAGE_CODE);
				if ( oldSynonyms == null ) {
					result.put(LATIN_LANGUAGE_CODE, synonyms);
				} else {
					oldSynonyms.addAll(synonyms);
				}
			}
			return result;
		}

		private List<String> extractSynonyms() throws ParsingException {
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

		protected Map<String, List<String>> extractVernacularNamesFromColumns()
				throws ParsingException {
			Map<String, List<String>> result = new HashMap<String, List<String>>();
			List<String> languageColumnNames = ((SpeciesCSVReader) getReader()).getLanguageColumnNames();
			for (String langCode : languageColumnNames) {
				List<String> vernacularNames = extractVernacularNames(langCode);
				result.put(langCode, vernacularNames);
			}
			return result;
		}

		protected List<String> extractVernacularNames(String colName) throws ParsingException {
			String colValue = StringUtils.normalizeSpace(getColumnValue(colName, false, String.class));
			if ( StringUtils.isBlank(colValue) ) {
				return Collections.emptyList();
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
						ParsingError error = new ParsingError(ErrorType.UNEXPECTED_SYNONYM, lineNumber, colName);
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
					column.getName(), "Error parsing " + fieldName +" from " + value);
			return error;
		}

		protected void throwEmptyColumnParsingException(SpeciesFileColumn column)
				throws ParsingException {
			ParsingError error = new ParsingError(ErrorType.EMPTY, lineNumber, column.getName());
			throw new ParsingException(error);
		}

	}
	
	class Validator {
		
		public void validate() throws ParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws ParsingException {
			List<String> colNames = getColumnNames();
			SpeciesFileColumn[] expectedColumns = SpeciesFileColumn.values();
			int fixedColsSize = expectedColumns.length;
			if ( colNames == null || colNames.size() < fixedColsSize ) {
				ParsingError error = new ParsingError(ErrorType.UNEXPECTED_COLUMNS);
				throw new ParsingException(error);
			}
			for (int i = 0; i < fixedColsSize; i++) {
				String colName = StringUtils.trimToEmpty(colNames.get(i));
				String expectedColName = expectedColumns[i].getName();
				if ( ! expectedColName.equals(colName) ) {
					ParsingError error = new ParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName, expectedColName);
					throw new ParsingException(error);
				}
			}
			validateLanguageHeaders(colNames);
		}

		protected void validateLanguageHeaders(List<String> colNames) {
			List<String> languageColumnNames = getLanguageColumnNames();
			for (String colName : languageColumnNames) {
				if ( ! Languages.exists(Languages.Standard.ISO_639_3, colName) ) {
					throw new RuntimeException("Invalid column name: " + colName + " - valid lanugage code (ISO-639-3) expected");
				}
			}
		}
		
	}

}
