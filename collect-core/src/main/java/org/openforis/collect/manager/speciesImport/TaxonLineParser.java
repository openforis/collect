package org.openforis.collect.manager.speciesImport;

import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;

import java.io.IOException;
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
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * 
 * @author S. Ricci
 *
 */
class TaxonLineParser {
	
	private static final String VERNACULAR_NAME_TRIM_EXPRESSION = "^\\s+|\\s+$|;+$|\\.+$";

	private static final String DEFAULT_VERNACULAR_NAMES_SEPARATOR = ",";
	private static final String OTHER_VERNACULAR_NAMES_SEPARATOR_EXPRESSION = "/";

	private CsvReader reader;
	private CsvLine currentLine;
	private int currentRowIndex;
	
	public TaxonLineParser(CsvReader reader) {
		super();
		this.currentRowIndex = -1;
		this.reader = reader;
	}

	public boolean isReady() {
		return currentLine != null;
	}

	public String getColumnValue(String column) {
		return currentLine.getValue(column, String.class);
	}

	public void readNextLine() throws IOException {
		currentLine = reader.readNextLine();
		currentRowIndex ++;
	}
	
	public List<String> getLanguageColumnNames() {
		List<String> columnNames = reader.getColumnNames();
		int fixedColumnsLength = TaxonFileColumn.values().length;
		if ( columnNames.size() > fixedColumnsLength ) {
			return columnNames.subList(fixedColumnsLength, columnNames.size());
		} else {
			return Collections.emptyList();
		}
	}
	
	public Integer parseTaxonId(boolean required) throws TaxonParsingException {
		Integer value = currentLine.getValue(TaxonFileColumn.ID.getName(), Integer.class);
		if ( required && value == null ) {
			throwEmptyColumnParsingException(TaxonFileColumn.ID);
		}
		return value;
	}
	
	public String parseCode() throws TaxonParsingException {
		String value = StringUtils.normalizeSpace(currentLine.getValue(TaxonFileColumn.CODE.getName(), String.class));
		if ( StringUtils.isBlank(value) ) {
			throwEmptyColumnParsingException(TaxonFileColumn.CODE);
		}
		return value;
	}
	
	public String parseFamilyName() throws TaxonParsingException {
		String value = StringUtils.normalizeSpace(currentLine.getValue(TaxonFileColumn.FAMILY.getName(), String.class));
		if ( StringUtils.isBlank(value) ) {
			throwEmptyColumnParsingException(TaxonFileColumn.FAMILY);
		}
		return value;
	}
	
	public String parseScientificName() throws TaxonParsingException {
		String value = StringUtils.normalizeSpace(currentLine.getValue(TaxonFileColumn.SCIENTIFIC_NAME.getName(), String.class));
		if ( StringUtils.isBlank(value) ) {
			throwEmptyColumnParsingException(TaxonFileColumn.SCIENTIFIC_NAME);
		}
		return value;
	}
	
	public TaxonRank parseRank() throws TaxonParsingException {
		String rawScientificName = parseScientificName();
		return parseRank(rawScientificName);
	}
	
	public TaxonRank parseRank(String rawScientificName) throws TaxonParsingException {
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
			throw new TaxonParsingException(new TaxonParsingError(currentRowIndex, TaxonFileColumn.SCIENTIFIC_NAME, "Error parsing rank from " + rawScientificName));
		}
	}

	public String parseCanonicalScientificName() throws TaxonParsingException {
		String rawName = parseScientificName();
		return parseCanonicalScientificName(rawName);
	}

	public String parseCanonicalScientificName(String rawScientificName) throws TaxonParsingException {
		try {
			NameParser nameParser = new NameParser();
			ParsedName<Object> parsedName;
			parsedName = nameParser.parse(rawScientificName);
			Rank rank = parsedName.getRank();
			boolean showRankMarker = rank == Rank.GENUS || rank == Rank.VARIETY;
			String result = parsedName.buildName(false, showRankMarker, false, false, false, true, true, false, false, false, false);
			return result;
		} catch (UnparsableException e) {
			throw new TaxonParsingException(new TaxonParsingError(currentRowIndex, TaxonFileColumn.SCIENTIFIC_NAME), e);
		}
	}
	
	public String parseGenus() throws TaxonParsingException {
		String rawScientificName = parseScientificName();
		return parseGenus(rawScientificName);
	}
	
	public String parseGenus(String rawScientificName) throws TaxonParsingException {
		try {
			NameParser nameParser = new NameParser();
			ParsedName<Object> parsedName;
			parsedName = nameParser.parse(rawScientificName);
			String genus = parsedName.getGenusOrAbove();
			return genus;
		} catch (UnparsableException e) {
			throw new TaxonParsingException(new TaxonParsingError(currentRowIndex, TaxonFileColumn.SCIENTIFIC_NAME, "Error parsing genus from " + rawScientificName));
		}
	}
	
	public String parseSpeciesName() throws TaxonParsingException {
		String rawScientificName = parseScientificName();
		return parseSpeciesName(rawScientificName);
	}
	
	public String parseSpeciesName(String rawScientificName) throws TaxonParsingException {
		try {
			NameParser nameParser = new NameParser();
			ParsedName<Object> parsedName;
			parsedName = nameParser.parse(rawScientificName);
			String speciesName = parsedName.canonicalSpeciesName();
			return speciesName;
		} catch (UnparsableException e) {
			throw new TaxonParsingException(new TaxonParsingError(currentRowIndex, TaxonFileColumn.SCIENTIFIC_NAME, "Error parsing species name from " + rawScientificName));
		}
	}

	public List<String> extractVernacularNames(String colName) {
		String colValue = getColumnValue(colName);
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

	public boolean isLeaf(TaxonRank rank) throws TaxonParsingException {
		String rawScientificName = parseScientificName();
		TaxonRank lineRank = parseRank(rawScientificName);
		return lineRank == rank;
	}

	public int getCurrentRowIndex() {
		return currentRowIndex;
	}
	
	protected void throwEmptyColumnParsingException(TaxonFileColumn column)
			throws TaxonParsingException {
		TaxonParsingError error = new TaxonParsingError(TaxonParsingError.Type.EMPTY, currentRowIndex + 1, column);
		throw new TaxonParsingException(error);
	}

	
}
