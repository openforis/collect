package org.openforis.collect.manager.speciesImport;

import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.speciesImport.TaxonCSVReader.TaxonLine;
import org.openforis.collect.manager.speciesImport.TaxonParsingError.Type;
import org.openforis.collect.manager.speciesImport.TaxonTree.Node;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;

/**
 * 
 * @author S. Ricci
 * 
 */
public class SpeciesImportProcess extends AbstractProcess<Void, SpeciesImportStatus> {

	private static Log LOG = LogFactory.getLog(SpeciesImportProcess.class);

	private static final TaxonRank[] TAXON_RANKS = new TaxonRank[] {FAMILY, GENUS, SPECIES, SUBSPECIES};

	private static final String CSV = "csv";
	//private static final String ZIP = "zip";

	private SpeciesManager speciesManager;
	private String taxonomyName;
	private File file;
	private boolean overwriteAll;
	
	private TaxonTree taxonTree;
	private List<String> languageColumnNames;
	private TaxonCSVReader reader;
	private String errorMessage;
	private List<TaxonLine> lines;
	
	public SpeciesImportProcess(SpeciesManager speciesManager, String taxonomyName, File file, boolean overwriteAll) {
		super();
		this.speciesManager = speciesManager;
		this.taxonomyName = taxonomyName;
		this.file = file;
		this.overwriteAll = overwriteAll;
	}
	
	@Override
	public void init() {
		super.init();
		taxonTree = new TaxonTree();
		lines = new ArrayList<TaxonLine>();
	}
	
	@Override
	protected void initStatus() {
		status = new SpeciesImportStatus(taxonomyName);
	}

	@Override
	public void startProcessing() throws Exception {
		super.startProcessing();
		processFile();
	}

	protected void processFile() throws IOException {
		String fileName = file.getName();
		String extension = FilenameUtils.getExtension(fileName);
		if ( CSV.equalsIgnoreCase(extension) ) {
			parseTaxonCSVLines(file);
//		} else if (ZIP.equals(extension) ) {
//			processPackagedFile();
//			status.complete();
		} else {
			errorMessage = "File type not supported" + extension;
			status.setErrorMessage(errorMessage);
			status.error();
			LOG.error("Species import: " + errorMessage);
		}
		if ( status.isRunning() ) {
			processLines();
		}
		if ( status.isRunning() && ! status.hasErrors() ) {
			persistTaxa();
		} else {
			status.error();
		}
		if ( status.isRunning() ) {
			status.complete();
		}
	}
	/*
	protected void processPackagedFile() throws IOException {
		File unzippedCsvFile = null;
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			InputStream is = zipFile.getInputStream(zipEntry);
			String unzippedPath = file.getAbsolutePath() + "_unzipped.csv";
			FileOutputStream os = new FileOutputStream(unzippedPath);
			IOUtils.copy(is, os);
			unzippedCsvFile = new File(unzippedPath);
			processCSV(unzippedCsvFile);
		}
		zipFile.close();
	}
	*/

	protected void parseTaxonCSVLines(File file) {
		InputStreamReader isReader = null;
		FileInputStream is = null;
		long currentRowNumber = 0;
		try {
			is = new FileInputStream(file);
			isReader = new InputStreamReader(is);
			
			reader = new TaxonCSVReader(isReader);
			reader.init();
			languageColumnNames = reader.getLanguageColumnNames();
			status.addProcessedRow(1);
			currentRowNumber = 2;
			while ( status.isRunning() ) {
				try {
					TaxonLine line = reader.readNextTaxonLine();
					if ( line != null ) {
						lines.add(line);
					}
					if ( ! reader.isReady() ) {
						break;
					}
				} catch (TaxonParsingException e) {
					status.addParsingError(currentRowNumber, e.getError());
				} finally {
					currentRowNumber ++;
				}
			}
			status.setTotal(reader.getLinesRead() + 1);
		} catch (TaxonParsingException e) {
			status.error();
			status.addParsingError(1, e.getError());
		} catch (Exception e) {
			status.error();
			status.addParsingError(currentRowNumber, new TaxonParsingError(Type.IOERROR, e.getMessage()));
			LOG.error("Error importing species CSV file", e);
		} finally {
			close(isReader);
		}
	}
	
	protected void processLines() {
		for (TaxonRank rank : TAXON_RANKS) {
			for (TaxonLine line : lines) {
				long lineNumber = line.getLineNumber();
				if ( ! status.isRowProcessed(lineNumber) && ! status.isRowInError(lineNumber) ) {
					try {
						boolean processed = processLine(line, rank);
						if (processed ) {
							status.addProcessedRow(lineNumber);
						}
					} catch (TaxonParsingException e) {
						status.addParsingError(lineNumber, e.getError());
					}
				}
			}
		}
	}
	
	protected boolean processLine(TaxonLine line, TaxonRank rank) throws TaxonParsingException {
		boolean mostSpecificRank = line.getRank() == rank;
		switch (rank) {
		case FAMILY:
			createTaxonFamily(line);
			return mostSpecificRank;
		case GENUS:
			createTaxonGenus(line);
			return mostSpecificRank;
		case SPECIES:
			createTaxonSpecies(line);
			return mostSpecificRank;
		default:
			Taxon parent = findParentTaxon(line);
			if ( parent == null ) {
				return false;
			} else {
				createTaxon(line, rank, parent);
				return true;
			}
		}
	}

	protected void processVernacularNames(TaxonLine line, Taxon taxon) {
		for (String langCode : languageColumnNames) {
			List<String> vernacularNames = line.getVernacularNames(langCode);
			for (String vernacularName : vernacularNames) {
				TaxonVernacularName taxonVN = new TaxonVernacularName();
				taxonVN.setLanguageCode(langCode);
				taxonVN.setVernacularName(StringUtils.normalizeSpace(vernacularName));
				taxonTree.addVernacularName(taxon, taxonVN);
			}
		}
	}

	protected void persistTaxa() {
		if ( overwriteAll ) {
			deleteOldTaxonomy();
		}
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

	protected void deleteOldTaxonomy() {
		Taxonomy oldTaxonomy = speciesManager.loadTaxonomyByName(taxonomyName);
		if ( oldTaxonomy != null ) {
			speciesManager.delete(oldTaxonomy);
		}
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

	private Taxon findParentTaxon(TaxonLine line) throws TaxonParsingException {
		TaxonRank rank = line.getRank();
		TaxonRank parentRank = rank.getParent();
		String scientificName;
		switch ( parentRank ) {
		case FAMILY:
			scientificName = line.getFamilyName();
			break;
		case GENUS:
			scientificName = line.getGenus();
			break;
		case SPECIES:
			scientificName = line.getSpeciesName();
			break;
		default:
			throw new RuntimeException("Unsupported rank");
		}
		Taxon result = taxonTree.findTaxonByScientificName(scientificName);
		return result;
	}
	
	protected Taxon createTaxonFamily(TaxonLine line) throws TaxonParsingException {
		String familyName = line.getFamilyName();
		return createTaxon(line, FAMILY, null, familyName);
	}
	
	protected Taxon createTaxonGenus(TaxonLine line) throws TaxonParsingException {
		Taxon taxonFamily = createTaxonFamily(line);
		String normalizedScientificName = line.getGenus();
		return createTaxon(line, GENUS, taxonFamily, normalizedScientificName);
	}

	protected Taxon createTaxonSpecies(TaxonLine line) throws TaxonParsingException {
		Taxon taxonGenus = createTaxonGenus(line);
		String normalizedScientificName = line.getSpeciesName();
		return createTaxon(line, SPECIES, taxonGenus, normalizedScientificName);
	}
	
	protected Taxon createTaxon(TaxonLine line, TaxonRank rank, Taxon parent) throws TaxonParsingException {
		String normalizedScientificName = line.getCanonicalScientificName();
		return createTaxon(line, rank, parent, normalizedScientificName);
	}
	
	protected Taxon createTaxon(TaxonLine line, TaxonRank rank, Taxon parent, String normalizedScientificName) throws TaxonParsingException {
		boolean mostSpecificRank = line.getRank() == rank;
		Taxon taxon = taxonTree.findTaxonByScientificName(normalizedScientificName);
		boolean newTaxon = taxon == null;
		if ( newTaxon ) {
			taxon = new Taxon();
			taxon.setTaxonRank(rank);
			taxon.setScientificName(normalizedScientificName);
			taxonTree.addNode(parent, taxon);
		} else if (mostSpecificRank) {
			taxonTree.checkDuplicateScienfificName(parent, normalizedScientificName, line.getLineNumber());
		}
		if ( mostSpecificRank ) {
			String code = line.getCode();
			Integer taxonId = line.getTaxonId();
			taxonTree.checkDuplicates(taxonId, code, line.getLineNumber());
			taxon.setCode(code);
			taxon.setTaxonId(taxonId);
			taxonTree.index(taxon);
			processVernacularNames(line, taxon);
		}
		return taxon;
	}

	private void close(Closeable closeable) {
		if ( closeable != null ) {
			try {
				closeable.close();
			} catch (IOException e) {
				LOG.error("Error closing stream: ", e);
			}
		}
	}
	
}
