/**
 * 
 */
package org.openforis.collect.manager.samplingDesignImport;

import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;

import java.io.FileNotFoundException;
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
import org.openforis.collect.manager.speciesImport.TaxonParsingError.ErrorType;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author S. Ricci
 *
 */
public class SamplingDesignCSVReader extends CsvReader {

	public enum Column {
		LEVEL_1(0, "level1_code"), LEVEL_2(1, "level2_code"), LOCATION(2, "location");
		
		private int index;
		private String name;
		
		private Column(int index, String name) {
			this.index = index;
			this.name = name;
		}
		
		public int getIndex() {
			return index;
		}
		
		public String getName() {
			return name;
		}
	}

	private CsvLine currentLine;
	private TaxonLine currentTaxonLine;

	/**
	 * @param filename
	 * @throws FileNotFoundException
	 */
	public SamplingDesignCSVReader(String filename) throws FileNotFoundException {
		super(filename);
	}

	/**
	 * @param reader
	 */
	public SamplingDesignCSVReader(Reader reader) {
		super(reader);
	}
	
	public void init() throws IOException, TaxonParsingException {
		readHeaders();
	}
	
	@Override
	public CsvLine readNextLine() throws IOException {
		throw new UnsupportedOperationException();
	}
	
	public TaxonLine readNextTaxonLine() throws TaxonParsingException, IOException {
		currentLine = super.readNextLine();
		currentTaxonLine = null;
		if ( currentLine != null ) {
			TaxonCSVLineParser lineParser = TaxonCSVLineParser.createInstance(this, currentLine);
			currentTaxonLine = lineParser.parse();
		}
		return currentTaxonLine;
	}
	
	public boolean validateAllFile() throws TaxonParsingException {
		Validator validator = new Validator();
		validator.validate();
		return true;
	}

	public List<String> getLanguageColumnNames() {
		List<String> columnNames = getColumnNames();
		int fixedColumnsLength = Column.values().length;
		if ( columnNames.size() > fixedColumnsLength ) {
			return columnNames.subList(fixedColumnsLength, columnNames.size());
		} else {
			return Collections.emptyList();
		}
	}
	
	public TaxonLine getCurrentTaxonLine() {
		return currentTaxonLine;
	}
	
	public boolean isReady() {
		return currentLine != null;
	}
	
	public static class TaxonCSVLineParser {
		private static final String VERNACULAR_NAME_TRIM_EXPRESSION = "^\\s+|\\s+$|;+$|\\.+$";
		private static final String SYNONYM_COL_NAME = "";
		private static final Pattern SYNONYM_PATTERN = Pattern.compile("^syn\\.?\\s+", Pattern.CASE_INSENSITIVE);
		
		private static final String DEFAULT_VERNACULAR_NAMES_SEPARATOR = ",";
		private static final String OTHER_VERNACULAR_NAMES_SEPARATOR_EXPRESSION = "/";

		private SamplingDesignCSVReader reader;
		private CsvLine csvLine;
		private long lineNumber;
		private ParsedName<Object> parsedScientificName;
		private String rawScientificName;
		
		TaxonCSVLineParser(SamplingDesignCSVReader reader, CsvLine line) {
			this.reader = reader;
			this.csvLine = line;
		
			this.lineNumber = reader.getLinesRead() + 1;
		}
		
		public static TaxonCSVLineParser createInstance(SamplingDesignCSVReader reader, CsvLine line) {
			return new TaxonCSVLineParser(reader, line);
		}
	
		public TaxonLine parse() throws TaxonParsingException {
			this.rawScientificName = extractRawScientificName();
			this.parsedScientificName = parseRawScienfificName();
			TaxonLine taxonLine = new TaxonLine();
			taxonLine.setLineNumber(this.lineNumber);
			taxonLine.setTaxonId(parseTaxonId(false));
			taxonLine.setCode(extractCode());
			taxonLine.setFamilyName(extractFamilyName());
			taxonLine.setRawScientificName(this.rawScientificName);
			taxonLine.setGenus(extractGenus());
			taxonLine.setSpeciesName(extractSpeciesName());
			taxonLine.setCanonicalScientificName(extractCanonicalScientificName());
			taxonLine.setRank(extractRank());
			taxonLine.setLanguageToVernacularNames(extractVernacularNames());
			return taxonLine;
		}

		protected Integer parseTaxonId(boolean required) throws TaxonParsingException {
			return getColumnValue(Column.NO, required, Integer.class);
		}

		protected String extractCode() throws TaxonParsingException {
			return getColumnValue(Column.CODE, true, String.class);
		}
		
		protected String extractFamilyName() throws TaxonParsingException {
			return getColumnValue(Column.FAMILY, true, String.class);
		}

		protected String extractRawScientificName() throws TaxonParsingException {
			return getColumnValue(Column.SCIENTIFIC_NAME, true, String.class);
		}
		
		protected ParsedName<Object> parseRawScienfificName() throws TaxonParsingException {
			try {
				NameParser nameParser = new NameParser();
				ParsedName<Object> parsedName = nameParser.parse(rawScientificName);
				return parsedName;
			} catch (UnparsableException e) {
				TaxonParsingError error = createFieldParsingError(
						Column.SCIENTIFIC_NAME, "scientific name", rawScientificName);
				throw new TaxonParsingException(error);
			}
		}
		
		protected TaxonRank extractRank() throws TaxonParsingException {
			Rank rank = parsedScientificName.getRank();
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

		protected String extractCanonicalScientificName() throws TaxonParsingException {
			Rank rank = parsedScientificName.getRank();
			boolean showRankMarker = rank == Rank.GENUS || rank == Rank.VARIETY;
			String result = parsedScientificName.buildName(false, showRankMarker, false, false, false, true, true, false, false, false, false);
			return result;
		}
		
		protected String extractGenus() throws TaxonParsingException {
			String genus = parsedScientificName.getGenusOrAbove();
			return genus;
		}
		
		protected String extractSpeciesName() throws TaxonParsingException {
			String speciesName = parsedScientificName.canonicalSpeciesName();
			return speciesName;
		}
		
		protected Map<String, List<String>> extractVernacularNames() throws TaxonParsingException {
			Map<String, List<String>> result = new HashMap<String, List<String>>();
			List<String> languageColumnNames = reader.getLanguageColumnNames();
			for (String langCode : languageColumnNames) {
				List<String> vernacularNames = extractVernacularNames(langCode);
				result.put(langCode, vernacularNames);
			}
			return result;
		}

		protected List<String> extractVernacularNames(String colName) throws TaxonParsingException {
			String colValue = StringUtils.normalizeSpace(csvLine.getValue(colName, String.class));
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

		private String extractVernacularName(String colName, String splitPart) throws TaxonParsingException {
			String trimmed = splitPart.replaceAll(VERNACULAR_NAME_TRIM_EXPRESSION, "");
			if ( trimmed.length() > 0 ) {
				Matcher matcher = SYNONYM_PATTERN.matcher(trimmed);
				if ( matcher.find() ) {
					if ( SYNONYM_COL_NAME.equals(colName) ) {
						matcher.replaceAll("");
					} else {
						TaxonParsingError error = new TaxonParsingError(ErrorType.UNEXPECTED_SYNONYM, lineNumber, colName);
						throw new TaxonParsingException(error);
					}
				}
				return trimmed;
			} else {
				return null;
			}
		}

		protected <T> T getColumnValue(Column column, boolean required, Class<T> type) throws TaxonParsingException {
			T value = csvLine.getValue(column.getName(), type);
			if ( required && ( value == null || value instanceof String && StringUtils.isBlank((String) value) )) {
				throwEmptyColumnParsingException(column);
			}
			return value;
		}
		
		protected TaxonParsingError createFieldParsingError(Column column, String fieldName, String value) {
			TaxonParsingError error = new TaxonParsingError(lineNumber, 
					column, "Error parsing " + fieldName +" from " + value);
			return error;
		}

		protected void throwEmptyColumnParsingException(Column column)
				throws TaxonParsingException {
			TaxonParsingError error = new TaxonParsingError(TaxonParsingError.ErrorType.EMPTY, lineNumber, column);
			throw new TaxonParsingException(error);
		}

		public SamplingDesignCSVReader getReader() {
			return reader;
		}
		
	}
	
	public static class TaxonLine {
		
		private long lineNumber;
		private Integer taxonId;
		private String code;
		private TaxonRank rank;
		private String familyName;
		private String genus;
		private String speciesName;
		private String rawScientificName;
		private String canonicalScientificName;
		private Map<String, List<String>> languageToVernacularNames;

		public List<String> getVernacularNames(String langCode) {
			if ( languageToVernacularNames != null ) {
				return languageToVernacularNames.get(langCode);
			} else {
				return null;
			}
		}

		public long getLineNumber() {
			return lineNumber;
		}

		public void setLineNumber(long lineNumber) {
			this.lineNumber = lineNumber;
		}

		public Integer getTaxonId() {
			return taxonId;
		}

		public void setTaxonId(Integer taxonId) {
			this.taxonId = taxonId;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public TaxonRank getRank() {
			return rank;
		}

		public void setRank(TaxonRank rank) {
			this.rank = rank;
		}

		public String getFamilyName() {
			return familyName;
		}

		public void setFamilyName(String familyName) {
			this.familyName = familyName;
		}

		public String getGenus() {
			return genus;
		}

		public void setGenus(String genus) {
			this.genus = genus;
		}

		public String getSpeciesName() {
			return speciesName;
		}

		public void setSpeciesName(String speciesName) {
			this.speciesName = speciesName;
		}

		public String getRawScientificName() {
			return rawScientificName;
		}

		public void setRawScientificName(String rawScientificName) {
			this.rawScientificName = rawScientificName;
		}

		public String getCanonicalScientificName() {
			return canonicalScientificName;
		}

		public void setCanonicalScientificName(String canonicalScientificName) {
			this.canonicalScientificName = canonicalScientificName;
		}

		public Map<String, List<String>> getLanguageToVernacularNames() {
			return languageToVernacularNames;
		}

		public void setLanguageToVernacularNames(
				Map<String, List<String>> languageToVernacularNames) {
			this.languageToVernacularNames = languageToVernacularNames;
		}
		
	}
	
	class Validator {
		
		public void validate() throws TaxonParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws TaxonParsingException {
			List<String> colNames = getColumnNames();
			Column[] expectedColumns = Column.values();
			int fixedColsSize = expectedColumns.length;
			if ( colNames == null || colNames.size() < fixedColsSize ) {
				TaxonParsingError error = new TaxonParsingError(ErrorType.UNEXPECTED_COLUMNS);
				throw new TaxonParsingException(error);
			}
			for (int i = 0; i < fixedColsSize; i++) {
				String colName = StringUtils.trimToEmpty(colNames.get(i));
				String expectedColName = expectedColumns[i].getName();
				if ( ! expectedColName.equals(colName) ) {
					TaxonParsingError error = new TaxonParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName, expectedColName);
					throw new TaxonParsingException(error);
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
