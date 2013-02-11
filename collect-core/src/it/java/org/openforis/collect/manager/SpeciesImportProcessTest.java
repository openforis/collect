package org.openforis.collect.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.manager.speciesImport.SpeciesImportProcess;
import org.openforis.collect.manager.speciesImport.SpeciesImportStatus;
import org.openforis.collect.manager.speciesImport.TaxonCSVReader.Column;
import org.openforis.collect.manager.speciesImport.TaxonParsingError;
import org.openforis.collect.manager.speciesImport.TaxonParsingError.ErrorType;
import org.openforis.collect.persistence.TaxonDao;
import org.openforis.collect.persistence.TaxonVernacularNameDao;
import org.openforis.collect.persistence.TaxonomyDao;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class SpeciesImportProcessTest {

	private static final String INVALID_TEST_SPECIES_CSV = "test-wrong-species.csv";
	private static final String VALID_TEST_SPECIES_CSV = "test-species.csv";

	private static final String TEST_TAXONOMY_NAME = "it_tree";
	
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private TaxonomyDao taxonomyDao;
	@Autowired
	private TaxonDao taxonDao;
	@Autowired
	private TaxonVernacularNameDao taxonVernacularNameDao;
	
	public SpeciesImportProcess importCSVFile(String fileName) throws Exception {
		File file = getTestFile(fileName);
		String taxonomyName = TEST_TAXONOMY_NAME;
		SpeciesImportProcess process = new SpeciesImportProcess(speciesManager, taxonomyName, file, true);
		process.call();
		return process;
	}
	
	@Test
	public void testSpeciesImport() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_TEST_SPECIES_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		
		String code = "AFZ/QUA";
		TaxonOccurrence occurrence = findByCode(code);
		TaxonOccurrence expected = new TaxonOccurrence(8, code, "Afzelia quanzensis");
		assertEquals(expected, occurrence);

		String code2 = "ALB/GLA";
		TaxonOccurrence occurrence2 = findByCode(code2);
		TaxonOccurrence expected2 = new TaxonOccurrence(11, code2, "Albizia glaberrima");
		assertEquals(expected2, occurrence2);
		
		String code3 = "ALB/SCH/amaniensis";
		TaxonOccurrence occurrence3 = findByCode(code3);
		TaxonOccurrence expected3 = new TaxonOccurrence(12, code3, "Albizia schimperiana var. amaniensis");
		assertEquals(expected3, occurrence3);
	}

	@Test
	public void testVernacularNamesImport() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_TEST_SPECIES_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		
		List<TaxonOccurrence> occurrences = speciesManager.findByVernacularName(TEST_TAXONOMY_NAME, "Mbamba", 10);
		assertNotNull(occurrences);
		assertEquals(1, occurrences.size());
		TaxonOccurrence stored = occurrences.get(0);
		TaxonOccurrence expected = new TaxonOccurrence(8, "AFZ/QUA", "Afzelia quanzensis", "Mbambakofi", "swh", null);
		assertEquals(expected, stored);
		
		List<TaxonOccurrence> occurrences2 = speciesManager.findByVernacularName(TEST_TAXONOMY_NAME, "Mshai-mamba", 10);
		assertNotNull(occurrences2);
		assertEquals(1, occurrences2.size());
		TaxonOccurrence stored2 = occurrences2.get(0);
		TaxonOccurrence expected2 = new TaxonOccurrence(10, "ALB/ADI", "Albizia adianthifolia", "Mshai-mamba", "ksb", null);
		assertEquals(expected2, stored2);
		
		Taxon taxon = findTaxonByCode("ALB/ADI");
		Integer taxonId = taxon.getSystemId();
		List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(taxonId);
		assertTrue(contains(vernacularNames, "ksb", "Mchao"));
		assertTrue(contains(vernacularNames, "ksb", "Mkengemshaa"));
		assertTrue(contains(vernacularNames, "ksb", "Msai"));
		assertTrue(contains(vernacularNames, "ksb", "Mshai"));
		assertTrue(contains(vernacularNames, "ksb", "Mshai-mamba"));
		
		assertFalse(contains(vernacularNames, "eng", "Mahogany"));
		assertFalse(contains(vernacularNames, "ksb", "Mahogany"));
	}

	@Test
	public void testHierarchyImport() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_TEST_SPECIES_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		
		Taxon subSpecies = findTaxonByCode("ALB/SCH/amaniensis");
		assertNotNull(subSpecies);
		assertEquals(SUBSPECIES, subSpecies.getTaxonRank());
		
		Integer speciesId = subSpecies.getParentId();
		assertNotNull(speciesId);
		Taxon species = taxonDao.loadById(speciesId);
		assertNotNull(species);
		assertNull(species.getCode());
		assertEquals(SPECIES, species.getTaxonRank());
		assertEquals("Albizia schimperiana", species.getScientificName());
		
		Integer genusId = species.getParentId();
		assertNotNull(genusId);
		Taxon genus = taxonDao.loadById(genusId);
		assertNotNull(genus);
		assertNull(genus.getCode());
		assertEquals(GENUS, genus.getTaxonRank());
		assertEquals("Albizia", genus.getScientificName());
		
		Integer familyId = genus.getParentId();
		assertNotNull(familyId);
		Taxon family = taxonDao.loadById(familyId);
		assertNotNull(family);
		assertNull(family.getParentId());
		assertEquals(FAMILY, family.getTaxonRank());
		assertEquals("Fabaceae", family.getScientificName());
	}
	
	@Test
	public void testErrorHandling() throws Exception {
		SpeciesImportProcess process = importCSVFile(INVALID_TEST_SPECIES_CSV);
		SpeciesImportStatus status = process.getStatus();
		List<TaxonParsingError> errors = status.getErrors();
		assertEquals(7, errors.size());
		assertFalse(status.isRowProcessed(3));
		assertFalse(status.isRowProcessed(4));
		assertFalse(status.isRowProcessed(6));
		assertFalse(status.isRowProcessed(7));
		assertFalse(status.isRowProcessed(8));
		assertFalse(status.isRowProcessed(9));
		assertFalse(status.isRowProcessed(10));
		assertTrue(status.isRowProcessed(1));
		assertTrue(status.isRowProcessed(2));
		assertTrue(status.isRowProcessed(5));
		
		assertTrue(containsError(errors, 3, Column.CODE, ErrorType.EMPTY));
		assertTrue(containsError(errors, 4, Column.SCIENTIFIC_NAME, ErrorType.EMPTY));
		assertTrue(containsError(errors, 6, Column.CODE, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 7, Column.FAMILY, ErrorType.EMPTY));
		assertTrue(containsError(errors, 8, Column.NO, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 9, Column.SCIENTIFIC_NAME, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 10, Column.SCIENTIFIC_NAME, ErrorType.DUPLICATE_VALUE));
	}

	protected boolean containsError(List<TaxonParsingError> errors, long row, Column column, ErrorType type) {
		for (TaxonParsingError taxonParsingError : errors) {
			if ( taxonParsingError.getErrorType() == type && taxonParsingError.getRow() == row && taxonParsingError.getColumn().equals(column.getName())) {
				return true;
			}
		}
		return false;
	}
	
	protected Taxon findTaxonByCode(String code) {
		Taxonomy taxonomy = taxonomyDao.load(TEST_TAXONOMY_NAME);
		List<Taxon> results = taxonDao.findByCode(taxonomy.getId(), code, 10);
		assertNotNull(results);
		assertEquals(1, results.size());
		Taxon taxon = results.get(0);
		return taxon;
	}
	
	protected TaxonOccurrence findByCode(String code) {
		List<TaxonOccurrence> occurrences = speciesManager.findByCode(TEST_TAXONOMY_NAME, code, 10);
		assertNotNull(occurrences);
		assertEquals(1, occurrences.size());
		TaxonOccurrence occurrence = occurrences.get(0);
		return occurrence;
	}
	
	protected boolean contains(List<TaxonVernacularName> taxonVernacularNames, String langCode, String vernacularName) {
		for (TaxonVernacularName taxonVernacularName : taxonVernacularNames) {
			if (vernacularName.equals(taxonVernacularName.getVernacularName()) && 
					langCode.equals(taxonVernacularName.getLanguageCode())) {
				return true;
			}
		}
		return false;
	}
	
	protected File getTestFile(String fileName) throws URISyntaxException {
		URL fileUrl = ClassLoader.getSystemResource(fileName);
		File file = new File(fileUrl.toURI());
		return file;
	}
}
