package org.openforis.collect.io.species;

import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.VARIETY;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.ReferenceDataImportTask;
import org.openforis.collect.io.metadata.species.SpeciesFileColumn;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.referencedataimport.ParsingException;
import org.openforis.collect.manager.speciesimport.SpeciesCSVReader;
import org.openforis.collect.manager.speciesimport.SpeciesLine;
import org.openforis.collect.model.TaxonTree;
import org.openforis.collect.model.TaxonTree.Node;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 * 
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SpeciesImportTask extends ReferenceDataImportTask<ParsingError> {

	private static final String TAXONOMY_NOT_FOUND_ERROR_MESSAGE_KEY = "speciesImport.error.taxonomyNotFound";
	private static final String INVALID_FAMILY_NAME_ERROR_MESSAGE_KEY = "speciesImport.error.invalidFamilyName";
	private static final String INVALID_GENUS_NAME_ERROR_MESSAGE_KEY = "speciesImport.error.invalidGenusName";
	private static final String INVALID_SPECIES_NAME_ERROR_MESSAGE_KEY = "speciesImport.error.invalidSpeciesName";
	private static final String INVALID_SCIENTIFIC_NAME_ERROR_MESSAGE_KEY = "speciesImport.error.invalidScientificNameName";
	
	private static final TaxonRank[] TAXON_RANKS = new TaxonRank[] {FAMILY, GENUS, SPECIES, SUBSPECIES, VARIETY};
	public static final String GENUS_SUFFIX = "sp.";

	@Autowired
	private SpeciesManager speciesManager;

	//parameters
	private int taxonomyId;
	private boolean overwriteAll;
	private transient File file;
	
	//runtime instance variables
	private transient TaxonTree taxonTree;
	private transient SpeciesCSVReader reader;
	private transient List<SpeciesLine> lines;
	
	public SpeciesImportTask() {
	}
	
	@Override
	public void init() {
		taxonTree = new TaxonTree();
		lines = new ArrayList<SpeciesLine>();
		validateParameters();
		try {
			reader = new SpeciesCSVReader(file);
		} catch (Exception e) {
			throw new RuntimeException("Error initializing task: " + e.getMessage(), e);
		}
		super.init();
	}
	
	protected void validateParameters() {
		if ( taxonomyId <= 0 ) {
			changeStatus(Status.FAILED);
			setErrorMessage(TAXONOMY_NOT_FOUND_ERROR_MESSAGE_KEY);
		}
	}
	
	@Override
	protected void execute() throws Throwable {
		parseTaxonCSVLines();
		if ( isRunning() ) {
			processLines();
		}
		if ( isRunning() && ! hasErrors() ) {
			persistTaxa();
		} else {
			changeStatus(Status.FAILED);
		}
	}
	
	@Override
	protected long countTotalItems() {
		try {
			return reader.size();
		} catch (IOException e) {
			throw new RuntimeException("Error counting total items: " + e.getMessage(), e);
		}
	}

	protected void parseTaxonCSVLines() {
		long currentRowNumber = 0;
		try {
			reader.init();
			addProcessedRow(1);
			currentRowNumber = 2;
			while ( isRunning() ) {
				try {
					SpeciesLine line = reader.readNextLine();
					if ( line != null ) {
						lines.add(line);
					}
					if ( ! reader.isReady() ) {
						break;
					}
				} catch (ParsingException e) {
					addParsingError(currentRowNumber, e.getError());
				} finally {
					currentRowNumber ++;
				}
			}
		} catch (ParsingException e) {
			addParsingError(1, e.getError());
			changeStatus(Status.FAILED);
		} catch (Exception e) {
			addParsingError(currentRowNumber, new ParsingError(ErrorType.IOERROR, e.getMessage()));
			changeStatus(Status.FAILED);
			log().error("Error importing species CSV file", e);
		}
	}
	
	protected void processLines() {
		for (TaxonRank rank : TAXON_RANKS) {
			for (SpeciesLine line : lines) {
				long lineNumber = line.getLineNumber();
				if ( ! isRowProcessed(lineNumber) && ! isRowInError(lineNumber) ) {
					try {
						boolean processed = processLine(line, rank);
						if (processed ) {
							addProcessedRow(lineNumber);
						}
					} catch (ParsingException e) {
						addParsingError(lineNumber, e.getError());
					}
				}
			}
		}
	}
	
	protected boolean processLine(SpeciesLine line, TaxonRank rank) throws ParsingException {
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
		case SUBSPECIES:
		case VARIETY:
			Taxon parent = findParentTaxon(line);
			if ( ! mostSpecificRank || parent == null ) {
				return false;
			} else {
				createTaxon(line, rank, parent);
				return true;
			}
		default: 
			return false;
		}
	}

	protected void processVernacularNames(SpeciesLine line, Taxon taxon) {
		Map<String, List<String>> langToVernacularName = line.getLanguageToVernacularNames();
		Set<String> vernacularLangCodes = langToVernacularName.keySet();
		for (String langCode : vernacularLangCodes) {
			List<String> vernacularNames = langToVernacularName.get(langCode);
			for (String vernacularName : vernacularNames) {
				TaxonVernacularName taxonVN = new TaxonVernacularName();
				taxonVN.setLanguageCode(langCode);
				taxonVN.setVernacularName(vernacularName);
				taxonTree.addVernacularName(taxon, taxonVN);
			}
		}
	}

	protected void persistTaxa() {
		speciesManager.insertTaxons(taxonomyId, taxonTree, overwriteAll);
	}

	private Taxon findParentTaxon(SpeciesLine line) throws ParsingException {
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
	
	protected Taxon createTaxonFamily(SpeciesLine line) throws ParsingException {
		String familyName = line.getFamilyName();
		if ( familyName == null ) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, line.getLineNumber(), SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName(), INVALID_FAMILY_NAME_ERROR_MESSAGE_KEY);
			throw new ParsingException(error);
		}
		return createTaxon(line, FAMILY, null, familyName);
	}
	
	protected Taxon createTaxonGenus(SpeciesLine line) throws ParsingException {
		String genus = line.getGenus();
		if ( genus == null ) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, line.getLineNumber(), SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName(), INVALID_GENUS_NAME_ERROR_MESSAGE_KEY);
			throw new ParsingException(error);
		}
		Taxon taxonFamily = createTaxonFamily(line);
		String normalizedScientificName = StringUtils.join(genus, " ", GENUS_SUFFIX);
		return createTaxon(line, GENUS, taxonFamily, normalizedScientificName);
	}

	protected Taxon createTaxonSpecies(SpeciesLine line) throws ParsingException {
		String speciesName = line.getSpeciesName();
		if ( speciesName == null ) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, line.getLineNumber(), SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName(), INVALID_SPECIES_NAME_ERROR_MESSAGE_KEY);
			throw new ParsingException(error);
		}
		Taxon taxonGenus = createTaxonGenus(line);
		return createTaxon(line, SPECIES, taxonGenus, speciesName);
	}
	
	protected Taxon createTaxon(SpeciesLine line, TaxonRank rank, Taxon parent) throws ParsingException {
		String normalizedScientificName = line.getCanonicalScientificName();
		if ( normalizedScientificName == null ) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, line.getLineNumber(), SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName(), INVALID_SCIENTIFIC_NAME_ERROR_MESSAGE_KEY);
			throw new ParsingException(error);
		}
		return createTaxon(line, rank, parent, normalizedScientificName);
	}
	
	protected Taxon createTaxon(SpeciesLine line, TaxonRank rank, Taxon parent, String normalizedScientificName) throws ParsingException {
		boolean mostSpecificRank = line.getRank() == rank;
		Taxon taxon = taxonTree.findTaxonByScientificName(normalizedScientificName);
		boolean newTaxon = taxon == null;
		if ( newTaxon ) {
			taxon = new Taxon();
			taxon.setTaxonRank(rank);
			taxon.setScientificName(normalizedScientificName);
			taxonTree.addNode(parent, taxon);
		} else if (mostSpecificRank) {
			checkDuplicateScientificName(line, parent, normalizedScientificName);
		}
		if ( mostSpecificRank ) {
			String code = line.getCode();
			Integer taxonId = line.getTaxonId();
			checkDuplicates(line, code, taxonId);
			taxon.setCode(code);
			taxon.setTaxonId(taxonId);
			taxonTree.updateNodeInfo(taxon);
			processVernacularNames(line, taxon);
		}
		return taxon;
	}

	protected void checkDuplicates(SpeciesLine line, String code,
			Integer taxonId) throws ParsingException {
		long lineNumber = line.getLineNumber();
		Node foundNode = null;
		foundNode = taxonTree.getNodeByTaxonId(taxonId);
		if ( foundNode != null ) {
			ParsingError error = new ParsingError(ErrorType.DUPLICATE_VALUE, lineNumber, SpeciesFileColumn.NO.getColumnName());
			throw new ParsingException(error);
		}
		foundNode = taxonTree.getNodeByCode(code);
		if ( foundNode != null ) {
			ParsingError error = new ParsingError(ErrorType.DUPLICATE_VALUE, lineNumber, SpeciesFileColumn.CODE.getColumnName());
			throw new ParsingException(error);
		}
	}

	protected void checkDuplicateScientificName(SpeciesLine line, Taxon parent,
			String normalizedScientificName) throws ParsingException {
		Node duplicateNode = taxonTree.getDuplicateScienfificNameNode(parent, normalizedScientificName);
		if ( duplicateNode != null ) {
			ParsingError error = new ParsingError(ErrorType.DUPLICATE_VALUE, line.getLineNumber(), SpeciesFileColumn.SCIENTIFIC_NAME.getColumnName());
			throw new ParsingException(error);
		}
	}

	public int getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(int taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}

	public boolean isOverwriteAll() {
		return overwriteAll;
	}

	public void setOverwriteAll(boolean overwriteAll) {
		this.overwriteAll = overwriteAll;
	}

}
