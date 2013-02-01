/**
 * 
 */
package org.openforis.collect.manager.speciesImport;

import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.gbif.ecat.parser.UnparsableException;
import org.gbif.ecat.voc.Rank;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author S. Ricci
 *
 */
public class TaxonCSVReader extends CsvReader {

	public enum Column {
		NO(0, "no"), CODE(1, "code"), FAMILY(2, "family"), SCIENTIFIC_NAME(3, "scientific_name");
		
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

	private TaxonCSVLine currentLine;

	/**
	 * @param filename
	 * @throws FileNotFoundException
	 */
	public TaxonCSVReader(String filename) throws FileNotFoundException {
		super(filename);
	}

	/**
	 * @param reader
	 */
	public TaxonCSVReader(Reader reader) {
		super(reader);
	}
	
	public void init() throws IOException, TaxonParsingException {
		readHeaders();
	}
	
	@Override
	public CsvLine readNextLine() throws IOException {
		throw new UnsupportedOperationException();
	}
	
	public TaxonCSVLine readNextTaxonLine() throws TaxonParsingException, IOException {
		CsvLine line = super.readNextLine();
		if ( line != null ) {
			currentLine = new TaxonCSVLine(this, line);
		} else {
			currentLine = null;
		}
		return currentLine;
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
	
	public TaxonCSVLine getCurrentLine() {
		return currentLine;
	}
	
	public boolean isReady() {
		return currentLine != null;
	}
	
	public static class TaxonCSVLine {
		
		private static final String VERNACULAR_NAME_TRIM_EXPRESSION = "^\\s+|\\s+$|;+$|\\.+$";

		private static final String DEFAULT_VERNACULAR_NAMES_SEPARATOR = ",";
		private static final String OTHER_VERNACULAR_NAMES_SEPARATOR_EXPRESSION = "/";

		private TaxonCSVReader reader;
		private CsvLine csvLine;
		private long rowNumber;

		private Integer taxonId;
		private String code;
		private TaxonRank rank;
		private String familyName;
		private String genus;
		private String speciesName;
		private String rawScientificName;
		private String canonicalScientificName;

		TaxonCSVLine(TaxonCSVReader reader, CsvLine line) throws TaxonParsingException {
			this.reader = reader;
			this.csvLine = line;
			this.rowNumber = reader.getLinesRead() + 1;
			
			parseValues();
		}
		
		protected void parseValues() throws TaxonParsingException {
			this.taxonId = parseTaxonId(false);
			this.code = parseCode();
			this.familyName = parseFamilyName();
			this.genus = parseGenus();
			this.speciesName = parseSpeciesName();
			this.rawScientificName = parseScientificName();
			this.canonicalScientificName = parseCanonicalScientificName();
			this.rank = parseRank();
		}

		protected Integer parseTaxonId(boolean required) throws TaxonParsingException {
			Integer value = csvLine.getValue(Column.NO.getName(), Integer.class);
			if ( required && value == null ) {
				throwEmptyColumnParsingException(Column.NO);
			}
			return value;
		}
		
		protected String parseCode() throws TaxonParsingException {
			return parseColumnValue(Column.CODE);
		}
		
		protected String parseFamilyName() throws TaxonParsingException {
			return parseColumnValue(Column.FAMILY);
		}

		protected String parseScientificName() throws TaxonParsingException {
			return parseColumnValue(Column.SCIENTIFIC_NAME);
		}
		
		protected TaxonRank parseRank() throws TaxonParsingException {
			String rawScientificName = parseScientificName();
			return parseRank(rawScientificName);
		}
		
		protected TaxonRank parseRank(String rawScientificName) throws TaxonParsingException {
			try {
				NameParser nameParser = new NameParser();
				ParsedName<Object> parsedName = nameParser.parse(rawScientificName);
				Rank rank = parsedName.getRank();
				TaxonRank taxonRank;
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
				return taxonRank;
			} catch (UnparsableException e) {
				TaxonParsingError error = createFieldParsingError(
						Column.SCIENTIFIC_NAME, "rank", rawScientificName);
				throw new TaxonParsingException(error);
			}
		}

		protected String parseCanonicalScientificName() throws TaxonParsingException {
			String rawName = parseScientificName();
			return parseCanonicalScientificName(rawName);
		}

		protected String parseCanonicalScientificName(String rawScientificName) throws TaxonParsingException {
			try {
				NameParser nameParser = new NameParser();
				ParsedName<Object> parsedName;
				parsedName = nameParser.parse(rawScientificName);
				Rank rank = parsedName.getRank();
				boolean showRankMarker = rank == Rank.GENUS || rank == Rank.VARIETY;
				String result = parsedName.buildName(false, showRankMarker, false, false, false, true, true, false, false, false, false);
				return result;
			} catch (UnparsableException e) {
				TaxonParsingError error = createFieldParsingError(Column.SCIENTIFIC_NAME, "canonical scientific name", rawScientificName);
				throw new TaxonParsingException(error, e);
			}
		}
		
		protected String parseGenus() throws TaxonParsingException {
			String rawScientificName = parseScientificName();
			return parseGenus(rawScientificName);
		}
		
		protected String parseGenus(String rawScientificName) throws TaxonParsingException {
			try {
				NameParser nameParser = new NameParser();
				ParsedName<Object> parsedName;
				parsedName = nameParser.parse(rawScientificName);
				String genus = parsedName.getGenusOrAbove();
				return genus;
			} catch (UnparsableException e) {
				TaxonParsingError error = createFieldParsingError(Column.SCIENTIFIC_NAME, "genus", rawScientificName);
				throw new TaxonParsingException(error);
			}
		}
		
		protected String parseSpeciesName() throws TaxonParsingException {
			String rawScientificName = parseScientificName();
			return parseSpeciesName(rawScientificName);
		}
		
		protected String parseSpeciesName(String rawScientificName) throws TaxonParsingException {
			try {
				NameParser nameParser = new NameParser();
				ParsedName<Object> parsedName;
				parsedName = nameParser.parse(rawScientificName);
				String speciesName = parsedName.canonicalSpeciesName();
				return speciesName;
			} catch (UnparsableException e) {
				TaxonParsingError error = createFieldParsingError(Column.SCIENTIFIC_NAME, "species name", rawScientificName);
				throw new TaxonParsingException(error);
			}
		}

		protected List<String> getVernacularNames(String colName) {
			String colValue = StringUtils.normalizeSpace(csvLine.getValue(colName, String.class));
			if ( StringUtils.isBlank(colValue) ) {
				return Collections.emptyList();
			} else {
				List<String> result = new ArrayList<String>();
				String normalized = colValue.replaceAll(OTHER_VERNACULAR_NAMES_SEPARATOR_EXPRESSION, DEFAULT_VERNACULAR_NAMES_SEPARATOR);
				String[] split = StringUtils.split(normalized, DEFAULT_VERNACULAR_NAMES_SEPARATOR);
				for (String splitPart : split) {
					String trimmed = splitPart.replaceAll(VERNACULAR_NAME_TRIM_EXPRESSION, "");
					if ( trimmed.length() > 0 ) {
						result.add(trimmed);
					}
				}
				return result;
			}
		}

		protected String parseColumnValue(Column column)
				throws TaxonParsingException {
			String value = StringUtils.normalizeSpace(csvLine.getValue(column.getName(), String.class));
			if ( StringUtils.isBlank(value) ) {
				throwEmptyColumnParsingException(column);
			}
			return value;
		}
		
		protected TaxonParsingError createFieldParsingError(Column column, String fieldName, String value) {
			TaxonParsingError error = new TaxonParsingError(rowNumber, 
					column, "Error parsing " + fieldName +" from " + value);
			return error;
		}

		protected void throwEmptyColumnParsingException(Column column)
				throws TaxonParsingException {
			TaxonParsingError error = new TaxonParsingError(TaxonParsingError.Type.EMPTY, rowNumber, column);
			throw new TaxonParsingException(error);
		}

		public TaxonCSVReader getReader() {
			return reader;
		}
		
		public Integer getTaxonId() {
			return taxonId;
		}

		public String getCode() {
			return code;
		}

		public TaxonRank getRank() {
			return rank;
		}

		public String getFamilyName() {
			return familyName;
		}
		
		public String getGenus() {
			return genus;
		}
		
		public String getRawScientificName() {
			return rawScientificName;
		}

		public String getCanonicalScientificName() {
			return canonicalScientificName;
		}

		public String getSpeciesName() {
			return speciesName;
		}
		
	}
	
	class Validator {
		
		public void validate() throws TaxonParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws TaxonParsingException {
			List<String> colNames = getColumnNames();
			Column[] columns = Column.values();
			int fixedColsSize = columns.length;
			if ( colNames == null || colNames.size() < fixedColsSize ) {
				String errorMessage = "Expected at least " + fixedColsSize + " columns";
				TaxonParsingError error = new TaxonParsingError(TaxonParsingError.Type.WRONG_HEADER, errorMessage);
				throw new TaxonParsingException(error);
			}
			for (int i = 0; i < fixedColsSize; i++) {
				String colName = colNames.get(i);
				String expectedColName = columns[i].getName();
				if ( ! expectedColName.equals(colName) ) {
					throw new RuntimeException("Invalid column name: " + colName + " - '"+ expectedColName +"' expected");
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
