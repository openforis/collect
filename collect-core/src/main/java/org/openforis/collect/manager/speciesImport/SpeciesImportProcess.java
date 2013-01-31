package org.openforis.collect.manager.speciesImport;

import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.speciesImport.TaxonTree.Node;
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

	private static final TaxonRank[] TAXON_RANKS = new TaxonRank[] {FAMILY, GENUS, SPECIES, SUBSPECIES};

	private static final String CSV = "csv";
	private static final String ZIP = "zip";

	private SpeciesManager speciesManager;
	private String taxonomyName;
	private File file;
	private String errorMessage;
	private SpeciesImportStatus status;
	
	private TaxonTree taxonTree;
	private List<Integer> processedLineNumbers;
	private TaxonLineParser lineReader;
	
	public SpeciesImportProcess(SpeciesManager speciesManager, String taxonomyName, File file) {
		super();
		this.speciesManager = speciesManager;
		this.taxonomyName = taxonomyName;
		this.file = file;
		status = new SpeciesImportStatus();
	}

	public void cancel() {
		status.cancel();
	}

	public SpeciesImportStatus getStatus() {
		return status;
	}
	
	@Override
	public Void call() throws Exception {
		status.start();
		prepare();
		processFile();
		if ( status.isRunning() ) {
			status.complete();
		}
		return null;
	}

	public void prepare() {
		processedLineNumbers = new ArrayList<Integer>();
		initCache();
	}

	protected void initCache() {
		taxonTree = new TaxonTree(TAXON_RANKS);
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
		for (TaxonRank rank : TAXON_RANKS) {
			if ( status.isRunning() ) {
				processCSV(file, rank);
			}
		}
		if ( status.isRunning() && status.getErrors().isEmpty() ) {
			persistTaxa();
		} else {
			status.error();
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
			lineReader = new TaxonLineParser(csvReader);
			validateHeaders(csvReader);
			lineReader.readNextLine();
			int count = 2;
			while ( lineReader.isReady() && status.isRunning() ) {
				try {
					if ( ! processedLineNumbers.contains(count) ) {
						boolean processed = processLine(rank);
						if (processed) {
							processedLineNumbers.add(count);
							status.rowProcessed();
						}
					}
					lineReader.readNextLine();
					count ++;
				} catch (TaxonParsingException e) {
					status.addError(count, e.getError());
				}
			}
		} catch (IOException e) {
			status.error();
			LOG.error("Error importing species CSV file", e);
		} catch (TaxonParsingException e) {
			status.addError(0, e.getError());
		} finally {
			close(reader);
		}
	}

	protected void validateHeaders(CsvReader csvReader) throws TaxonParsingException {
		List<String> colNames = csvReader.getColumnNames();
		TaxonFileColumn[] columns = TaxonFileColumn.values();
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
		List<String> languageColumnNames = lineReader.getLanguageColumnNames();
		for (String colName : languageColumnNames) {
			if ( ! Languages.exists(Languages.Standard.ISO_639_3, colName) ) {
				throw new RuntimeException("Invalid column name: " + colName + " - valid lanugage code (ISO-639-3) expected");
			}
		}
	}
	
	protected boolean processLine(TaxonRank rank) throws TaxonParsingException {
		boolean leaf = lineReader.isLeaf(rank);
		switch (rank) {
		case FAMILY:
			createTaxonFamily();
			return leaf;
		case GENUS:
			createTaxonGenus();
			return leaf;
		case SPECIES:
			createTaxonSpecies();
			return leaf;
		default:
			Taxon parent = findParentTaxon();
			if ( parent == null ) {
				return false;
			} else {
				createTaxon(rank, parent);
				return true;
			}
		}
	}

	protected void processVernacularNames(Taxon taxon) {
		List<String> languageColumnNames = lineReader.getLanguageColumnNames();
		for (String langCode : languageColumnNames) {
			List<String> vernacularNames = lineReader.extractVernacularNames(langCode);
			for (String vernacularName : vernacularNames) {
				TaxonVernacularName taxonVN = new TaxonVernacularName();
				taxonVN.setLanguageCode(langCode);
				taxonVN.setVernacularName(normalize(vernacularName));
				taxonTree.addVernacularName(taxon, taxonVN);
			}
		}
	}

	protected void persistTaxa() {
		Taxonomy taxonomy = new Taxonomy();
		taxonomy.setName(taxonomyName);
		speciesManager.save(taxonomy);
		final Integer taxonomyId = taxonomy.getId();
		taxonTree.bfs(new TaxonTree.NodeVisitor() {
			@Override
			public void visit(Node node) {
				persistTaxonTreeNode(taxonomyId, node);
			}
		});
	}

	protected void persistTaxonTreeNode(Integer taxonomyId, Node node) {
		Taxon taxon = node.getTaxon();
		taxon.setTaxonomyId(taxonomyId);
		Node parent = node.getParent();
		if ( parent != null ) {
			Taxon parentTaxon = parent.getTaxon();
			taxon.setParentId(parentTaxon.getSystemId());
			taxon.setStep(9);
		}
		speciesManager.save(taxon);
		
		List<TaxonVernacularName> vernacularNames = node.getVernacularNames();
		if ( vernacularNames != null ) {
			for (TaxonVernacularName vernacularName : vernacularNames) {
				vernacularName.setTaxonSystemId(taxon.getSystemId());
				speciesManager.save(vernacularName);
			}
		}
	}

	private String normalize(String vernacularName) {
		return StringUtils.normalizeSpace(vernacularName);
	}

	private Taxon findParentTaxon() throws TaxonParsingException {
		TaxonRank rank = lineReader.parseRank();
		TaxonRank parentRank = rank.getParent();
		String scientificName;
		switch ( parentRank ) {
		case FAMILY:
			scientificName = lineReader.parseFamilyName();
			break;
		case GENUS:
			scientificName = lineReader.parseGenus();
			break;
		case SPECIES:
			scientificName = lineReader.parseSpeciesName();
			break;
		default:
			throw new RuntimeException("Unsupported rank");
		}
		Taxon result = taxonTree.findTaxon(parentRank, scientificName);
		return result;
	}
	
	protected Taxon createTaxonFamily() throws TaxonParsingException {
		String familyName = lineReader.parseFamilyName();
		return createTaxon(FAMILY, null, familyName);
	}
	
	protected Taxon createTaxonGenus() throws TaxonParsingException {
		Taxon taxonFamily = createTaxonFamily();
		String rawScientificName = lineReader.parseScientificName();
		String normalizedScientificName = lineReader.parseGenus(rawScientificName);
		return createTaxon(GENUS, taxonFamily, normalizedScientificName);
	}

	protected Taxon createTaxonSpecies() throws TaxonParsingException {
		Taxon taxonGenus = createTaxonGenus();
		String normalizedScientificName = lineReader.parseSpeciesName();
		return createTaxon(SPECIES, taxonGenus, normalizedScientificName);
	}
	
	protected Taxon createTaxon(TaxonRank rank, Taxon parent) throws TaxonParsingException {
		String normalizedScientificName = lineReader.parseCanonicalScientificName();
		return createTaxon(rank, parent, normalizedScientificName);
	}
	
	protected Taxon createTaxon(TaxonRank rank, Taxon parent, String normalizedScientificName) throws TaxonParsingException {
		Taxon taxon = taxonTree.findTaxon(rank, normalizedScientificName);
		if ( taxon == null ) {
			taxon = new Taxon();
			taxon.setTaxonRank(rank);
			taxon.setScientificName(normalizedScientificName);
			taxonTree.addNode(parent, taxon);
		}
		if ( lineReader.isLeaf(rank) ) {
			String code = lineReader.parseCode();
			Integer taxonId = lineReader.parseTaxonId(false);
			taxon.setCode(code);
			taxon.setTaxonId(taxonId);
			processVernacularNames(taxon);
		}
		return taxon;
	}

	private void close(InputStreamReader reader) {
		if ( reader != null ) {
			try {
				reader.close();
			} catch (IOException e) {
				LOG.error("Error closing reader", e);
			}
		}
	}
}
