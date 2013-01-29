package org.openforis.collect.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.gbif.ecat.parser.UnparsableException;
import org.gbif.ecat.voc.Rank;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;

/**
 * 
 * @author S. Ricci
 * 
 */
public class SpeciesImportProcess implements Callable<Void> {

	private static Log LOG = LogFactory.getLog(SpeciesImportProcess.class);
	
	private static final String CSV = "csv";
	private static final String ZIP = "zip";
	private static final String VERNACULAR_NAMES_SEPARATOR = ",";

	enum Step {
		STOPPED, PREPARING, RUNNING, COMPLETE, CANCELLED, ERROR
	}
	
	private SpeciesManager speciesManager;
	private String taxonomyName;
	private File file;
	private String errorMessage;
	private Step step;
	
	private Map<TaxonRank, Map<String, Taxon>> taxons;
	private List<Integer> processedLineNumbers;
	
	public SpeciesImportProcess(SpeciesManager speciesManager, String taxonomyName, File file) {
		super();
		this.speciesManager = speciesManager;
		this.taxonomyName = taxonomyName;
		this.file = file;
		step = Step.STOPPED;
	}

	public void cancel() {
		step = Step.CANCELLED;
	}

	public boolean isRunning() {
		return step == Step.RUNNING;
	}

	public boolean isComplete() {
		return step == Step.COMPLETE;
	}

	public Step getStep() {
		return step;
	}

	@Override
	public Void call() throws Exception {
		step = Step.RUNNING;
		prepare();
		processFile();
		if ( step == Step.RUNNING ) {
			step = Step.COMPLETE;
		}
		return null;
	}

	protected void prepare() {
		processedLineNumbers = new ArrayList<Integer>();
		initCache();
	}

	protected void initCache() {
		taxons = new HashMap<Taxon.TaxonRank, Map<String,Taxon>>();
		TaxonRank[] ranks = Taxon.TaxonRank.values();
		for (TaxonRank rank : ranks) {
			taxons.put(rank, new HashMap<String, Taxon>());
		}
	}
	
	protected void processFile() throws IOException {
		String fileName = file.getName();
		String extension = FilenameUtils.getExtension(fileName);
		if ( CSV.equalsIgnoreCase(extension) ) {
			processCSV(file);
		} else if (ZIP.equals(extension) ) {
			processPackagedFile();
		} else {
			errorMessage = "File type not supported" + extension;
			LOG.error(errorMessage);
		}
	}

	protected void processPackagedFile() throws IOException {
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			InputStream is = zipFile.getInputStream(zipEntry);
			String unzippedPath = file.getAbsolutePath() + "_unzipped.csv";
			FileOutputStream os = new FileOutputStream(unzippedPath);
			IOUtils.copy(is, os);
			processCSV(new File(unzippedPath));
		}
		zipFile.close();
	}

	protected void processCSV(File file) {
		TaxonRank[] ranks = new TaxonRank[] {TaxonRank.FAMILY, TaxonRank.GENUS, TaxonRank.SPECIES};
		for (TaxonRank rank : ranks) {
			if ( step == Step.RUNNING ) {
				processCSV(file, rank);
			}
		}
		if ( step == Step.RUNNING ) {
			persistTaxa();
		}
	}

	protected void processCSV(File file, TaxonRank rank) {
		FileInputStream is = null;
		InputStreamReader reader = null;
		try {
			is = new FileInputStream(file);
			reader = new InputStreamReader(is);
			CsvReader csvReader = new CsvReader(reader);
			csvReader.readHeaders();
			CsvLine line = csvReader.readNextLine();
			int count = 1;
			while ( line != null ) {
				if ( ! processedLineNumbers.contains(count) ) {
					boolean processed = processLine(rank, line);
					if (processed) {
						processedLineNumbers.add(count);
					}
				}
				line = csvReader.readNextLine();
				count ++;
			}
		} catch (Exception e) {
			step = Step.ERROR;
			LOG.error("Error importing species CSV file", e);
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
			if ( is != null ) {
				try {
					is.close();
				} catch (IOException e) {}
			}
		}
	}
	
	protected boolean processLine(TaxonRank rank, CsvLine line) {
		String code = normalize(line.getValue(0, String.class));
		String familyName = normalize(line.getValue(1, String.class));
		String scientificName = normalize(line.getValue(2, String.class));
		if ( rank == TaxonRank.FAMILY ) {
			createTaxonFamily(familyName, line);
			return true;
		} else if ( rank == TaxonRank.GENUS ) {
			createTaxonGenus(scientificName, line);
			return false;
		} else {
			Taxon parent = findParentTaxon(scientificName);
			if ( parent == null ) {
				return false;
			} else {
				Taxon taxon = new Taxon();
				taxon.setCode(code);
				taxon.setScientificName(scientificName);
				taxon.setTaxonRank(rank);
				taxon.setParentId(parent.getSystemId());
				processVernacularNames(taxon, line);
			}
			return true;
		}
	}

	protected void processVernacularNames(Taxon taxon, CsvLine line) {
		List<String> columnNames = line.getColumnNames();
		for ( int i = 3; i < columnNames.size(); i++ ) {
			String langCode = columnNames.get(i);
			if ( Languages.exists(Languages.Standard.ISO_639_3, langCode) ) {
				String colValue = line.getValue(i, String.class);
				List<String> vernacularNames = extractVernacularNames(colValue);
				for (String vernacularName : vernacularNames) {
					TaxonVernacularName taxonVN = new TaxonVernacularName();
					taxonVN.setLanguageCode(langCode);
					taxonVN.setTaxonSystemId(taxon.getSystemId());
					taxonVN.setVernacularName(normalize(vernacularName));
				}
			} else {
				step = Step.ERROR;
				errorMessage = "Invalid language code: " + langCode;
			}
		}
	}

	protected void persistTaxa() {
		Taxonomy taxonomy = new Taxonomy();
		taxonomy.setName(taxonomyName);
		speciesManager.save(taxonomy);
		Integer taxonomyId = taxonomy.getId();
		TaxonRank[] ranks = new TaxonRank[]{TaxonRank.FAMILY, TaxonRank.GENUS, TaxonRank.SPECIES};
		for (TaxonRank rank : ranks) {
			Map<String, Taxon> taxaPerRank = taxons.get(rank);
			Collection<Taxon> taxa = taxaPerRank.values();
			for (Taxon taxon : taxa) {
				taxon.setTaxonId(taxonomyId);
				speciesManager.save(taxon);
			}
		}
	}

	private List<String> extractVernacularNames(String value) {
		String normalized = value.replaceAll("\\.\\/", VERNACULAR_NAMES_SEPARATOR);
		String[] split = StringUtils.split(normalized, VERNACULAR_NAMES_SEPARATOR);
		List<String> result = new ArrayList<String>();
		for (String splitPart : split) {
			String trimmed = splitPart.replaceAll("^\\s+|\\s+$|;+$|\\.$", "");
			if ( trimmed.length() > 0 ) {
				result.add(trimmed);
			}
		}
		return result;
	}

	private String normalize(String vernacularName) {
		return StringUtils.normalizeSpace(vernacularName);
	}

	private Taxon findParentTaxon(String scientificName) {
		try {
			TaxonRank taxonRank = parseRank(scientificName);
			Map<String, Taxon> familyTaxa = taxons.get(taxonRank);
			Taxon result = familyTaxa.get(scientificName);
			return result;
		} catch (UnparsableException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected Taxon createTaxonFamily(String familyName, CsvLine line) {
		Map<String, Taxon> familyTaxons = taxons.get(Taxon.TaxonRank.FAMILY);
		Taxon oldTaxon = familyTaxons.get(familyName);
		if ( oldTaxon != null ) {
			return oldTaxon;
		} else {
			Taxon taxon = new Taxon();
			taxon.setScientificName(familyName);
			processVernacularNames(taxon, line);
			familyTaxons.put(familyName, taxon);
			return taxon;
		}
	}
	
	protected Taxon createTaxonGenus(String scientificName, CsvLine line) {
		String genus = parseGenus(scientificName);
		Map<String, Taxon> genusTaxons = taxons.get(Taxon.TaxonRank.GENUS);
		Taxon oldTaxon = genusTaxons.get(genus);
		if ( oldTaxon != null ) {
			return oldTaxon;
		} else {
			Taxon taxon = new Taxon();
			taxon.setScientificName(genus);
			processVernacularNames(taxon, line);
			genusTaxons.put(genus, taxon);
			return taxon;
		}
	}

	protected TaxonRank parseRank(String scientificName)
			throws UnparsableException {
		NameParser nameParser = new NameParser();
		ParsedName<Object> parsedName = nameParser.parse(scientificName);
		Rank rank = parsedName.getRank();
		TaxonRank taxonRank;
		switch ( rank ) {
		case FAMILY:
			taxonRank = TaxonRank.FAMILY;
			break;
		case GENUS:
			taxonRank = TaxonRank.GENUS;
			break;
		default:
			taxonRank = TaxonRank.SPECIES;
		}
		return taxonRank;
	}

	protected String parseGenus(String scientificName) {
		try {
			NameParser nameParser = new NameParser();
			ParsedName<Object> parsedName;
			parsedName = nameParser.parse(scientificName);
			String genus = parsedName.getGenusOrAbove();
			return genus;
		} catch (UnparsableException e) {
			LOG.error("Error extracting genus from: " + scientificName, e);
			throw new RuntimeException(e);
		}
	}

}
