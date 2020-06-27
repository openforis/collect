/**
 * 
 */
package org.openforis.collect.manager.speciesimport;

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
import org.openforis.collect.io.metadata.species.SpeciesFileColumn;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author S. Ricci
 *
 */
public class SpeciesCSVReader extends CSVReferenceDataImportReader<SpeciesLine> {

	public SpeciesCSVReader(File file, CSVFileOptions csvFileOptions) throws IOException, ParsingException {
		super(file, csvFileOptions);
	}
	
	@Override
	protected boolean isInfoAttribute(String col) {
		Set<String> predefinedColumnNames = new HashSet<String>();
		for (SpeciesFileColumn column : SpeciesFileColumn.values()) {
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
	
	public static class SpeciesCSVLineParser extends CSVReferenceDataLineParser<SpeciesLine> {
		
		public static final String GENUS_SUFFIX = "sp.";
		private static final String GENUS_PATTERN_STR = "[A-Z][a-z]+";
		private static final String SPECIES_NAME_PATTERN_STR = "[a-z]+\\-?[a-z]+";
		private static final String DEFAULT_HYBRID_FORMULA = "×";
		private static final Pattern ONLY_GENUS_PATTERN = 
				Pattern.compile("^(" + GENUS_PATTERN_STR + ")(\\s+(spp|sp)\\.?)?$");
		private static final String CAPITALIZED_WORD_PATTERN_STR = "[A-Z][a-z]+";
		private static final String CULTIVAR_NAME_PATTERN_STR = "'" + CAPITALIZED_WORD_PATTERN_STR + "(\\s+" + CAPITALIZED_WORD_PATTERN_STR + ")*'";
		private static final String SUBSPECIES_NAME_PATTERN_STR = "[a-z]+|[a-z]+\\-[a-z]+";
		private static final Pattern SPECIES_AND_ABOVE_PATTERN = 
				Pattern.compile("^(" + GENUS_PATTERN_STR + ")\\s+(" + SPECIES_NAME_PATTERN_STR + ")(\\s+(ssp|subsp|var|f)\\.?\\s+(" + SUBSPECIES_NAME_PATTERN_STR + "))?$");
		private static final Pattern CULTIVAR_SPECIES_PATTERN = 
				Pattern.compile("^(" + GENUS_PATTERN_STR + ")\\s+(" + SPECIES_NAME_PATTERN_STR + ")\\s+(" + CULTIVAR_NAME_PATTERN_STR + ")$");
		private static final Pattern HYBRID_SPECIES_PATTERN_1 = 
				Pattern.compile("^(" + GENUS_PATTERN_STR + ")\\s+([x|X|" + DEFAULT_HYBRID_FORMULA +"])\\s+(" + SPECIES_NAME_PATTERN_STR + ")$"); //e.g. Annona x atemoya
		private static final Pattern HYBRID_SPECIES_PATTERN_2 = 
				Pattern.compile("^(" + GENUS_PATTERN_STR + ")\\s+" + DEFAULT_HYBRID_FORMULA +"(" + SPECIES_NAME_PATTERN_STR + ")$"); //e.g. Annona ×atemoya
		private static final Pattern HYBRID_SPECIES_PATTERN_3 = 
				Pattern.compile("^(" + GENUS_PATTERN_STR + ")\\s+(" + SPECIES_NAME_PATTERN_STR + ")\\s+"
						+ "([x|X|" + DEFAULT_HYBRID_FORMULA +"])\\s+(([A-Z]\\.)|(" + GENUS_PATTERN_STR + "))?\\s*(" + SPECIES_NAME_PATTERN_STR + ")$"); //e.g. Citrus reticulata x Citrus paradisi
		
		private static final String VERNACULAR_NAME_TRIM_EXPRESSION = "^\\s+|\\s+$|;+$|\\.+$";
		private static final String SYNONYM_COL_NAME = "synonyms";
		private static final String SYNONYM_SPLIT_EXPRESSION = "((syn|Syn)(\\.\\:|\\.|\\:|\\s))";
		private static final Pattern SYNONYM_PATTERN = Pattern.compile("^" + SYNONYM_SPLIT_EXPRESSION, Pattern.CASE_INSENSITIVE);

		private static final String DEFAULT_VERNACULAR_NAMES_SEPARATOR = ",";
		private static final String OTHER_VERNACULAR_NAMES_SEPARATOR_EXPRESSION = "/";
		
		public static final String UNEXPECTED_SYNONYM_MESSAGE_KEY = "survey.taxonomy.import_data.error.unexpected_synonym";
		
		SpeciesCSVLineParser(SpeciesCSVReader reader, CsvLine line, List<String> infoColumnNames) {
			super(reader, line, infoColumnNames);
		}
		
		public static SpeciesCSVLineParser createInstance(SpeciesCSVReader reader, CsvLine line, List<String> infoColumnNames) {
			return new SpeciesCSVLineParser(reader, line, infoColumnNames);
		}
	
		public SpeciesLine parse() throws ParsingException {
			SpeciesLine line = super.parse();
			line.setTaxonId(extractTaxonId(false));
			line.setCode(extractCode(true));
			line.setFamilyName(extractFamilyName());
			line.setLanguageToVernacularNames(extractLanguageToVernacularNames());
			
			String rawScientificName = extractRawScientificName();
			rawScientificName = StringUtils.trimToNull(rawScientificName);
			
			//family rank
			if (rawScientificName == null) {
				line.setCanonicalScientificName(line.getFamilyName());
				line.setRank(TaxonRank.FAMILY);
				return line;
			}
			//rank depends on scientific name
			ScientificNameParseResult scientificNameParseResult = parseScientificName(rawScientificName);
			line.setGenus(scientificNameParseResult.getGenus());
			line.setSpeciesName(scientificNameParseResult.getSpeciesName());
			line.setCanonicalScientificName(scientificNameParseResult.getCanonicalScientificName());
			line.setRank(scientificNameParseResult.getRank());
			return line;
		}
		
		private ScientificNameParseResult parseScientificName(String rawScientificName) throws ParsingException {
			//genus rank
			{
				Matcher matcher = ONLY_GENUS_PATTERN.matcher(rawScientificName);
				if (matcher.matches()) {
					String genus = matcher.group(1);
					return new ScientificNameParseResult(genus, null, genus + " " + GENUS_SUFFIX, TaxonRank.GENUS);
				}
			}
			//species rank and above
			{
				Matcher matcher = SPECIES_AND_ABOVE_PATTERN.matcher(rawScientificName);
				if (matcher.matches()) {
					String genus = matcher.group(1);
					String species = matcher.group(2);
					String speciesName = genus + " " + species;
					String canonicalScientificName = speciesName;
					
					String discriminator = matcher.group(4);
					TaxonRank rank;
					if (StringUtils.isBlank(discriminator)) {
						rank = TaxonRank.SPECIES;
					} else {
						String normalizedDiscriminator;
						if ("var".equals(discriminator)) {
							rank = TaxonRank.VARIETY;
							normalizedDiscriminator = "var.";
						} else if ("subsp".equals(discriminator) || "ssp".equals(discriminator)) {
							rank = TaxonRank.SUBSPECIES;
							normalizedDiscriminator = "subsp.";
						} else if ("f".equals(discriminator)) {
							rank = TaxonRank.FORM;
							normalizedDiscriminator = "f.";
						} else {
							throw new IllegalArgumentException(String.format("Invalid discriminator %s found in scientific name %s", 
									discriminator, rawScientificName));
						}
						canonicalScientificName += " " + normalizedDiscriminator + " " + matcher.group(5); 
					}
					return new ScientificNameParseResult(genus, speciesName, canonicalScientificName, rank);
				}
			}
			{
				Matcher matcher = HYBRID_SPECIES_PATTERN_1.matcher(rawScientificName);
				if (matcher.find()) {
					String genus = matcher.group(1);
					String species = matcher.group(3);
					String hybridFormula = matcher.group(2).toLowerCase(Locale.ENGLISH);
					String speciesName = genus + " " + hybridFormula + " " + species;
					return new ScientificNameParseResult(genus, speciesName, speciesName, TaxonRank.SPECIES);
				}
			}
			{
				Matcher matcher = HYBRID_SPECIES_PATTERN_2.matcher(rawScientificName);
				if (matcher.find()) {
					String genus = matcher.group(1);
					String species = matcher.group(2);
					String speciesName = genus + " " + DEFAULT_HYBRID_FORMULA + species;
					return new ScientificNameParseResult(genus, speciesName, speciesName, TaxonRank.SPECIES);
				}
			}
			{
				Matcher matcher = HYBRID_SPECIES_PATTERN_3.matcher(rawScientificName);
				if (matcher.find()) {
					String genus = matcher.group(1);
					String species = matcher.group(2);
					String hybridFormula = matcher.group(3).toLowerCase(Locale.ENGLISH);
					String secondaryGenus = matcher.group(6);
					String secondarySpecies = matcher.group(7);
					String speciesName = genus + " " + species + " " + hybridFormula + " " + 
							(secondaryGenus == null ? "" : secondaryGenus + " ") +
							secondarySpecies;
					return new ScientificNameParseResult(genus, speciesName, speciesName, TaxonRank.SPECIES);
				}
			}
			{
				Matcher matcher = CULTIVAR_SPECIES_PATTERN.matcher(rawScientificName);
				if (matcher.find()) {
					String genus = matcher.group(1);
					String species = matcher.group(2);
					String cultivarName = matcher.group(3);
					String speciesName = genus + " " + species;
					String canonicalScientificName = speciesName + " " + cultivarName;
					return new ScientificNameParseResult(genus, speciesName, canonicalScientificName, TaxonRank.CULTIVAR);
				}
			}
			ParsingError error = createFieldParsingError(
					SpeciesFileColumn.SCIENTIFIC_NAME, "scientific name", rawScientificName);
			throw new ParsingException(error);
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
		
		protected Map<String, List<String>> extractLanguageToVernacularNames() throws ParsingException {
			VernacularLanguagesMap result = extractVernacularNamesFromColumns();
			return result.getMap();
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
	
	private static class ScientificNameParseResult {
		private String genus;
		private String speciesName;
		private String canonicalScientificName;
		private TaxonRank rank;
		
		public ScientificNameParseResult(String genus, String speciesName, String canonicalScientificName, TaxonRank rank) {
			super();
			this.genus = genus;
			this.speciesName = speciesName;
			this.canonicalScientificName = canonicalScientificName;
			this.rank = rank;
		}
		
		public String getGenus() {
			return genus;
		}
		
		public String getSpeciesName() {
			return speciesName;
		}
		
		public String getCanonicalScientificName() {
			return canonicalScientificName;
		}
		
		public TaxonRank getRank() {
			return rank;
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
