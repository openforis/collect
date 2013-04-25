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
import static org.openforis.idm.model.species.Taxon.TaxonRank.VARIETY;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.speciesimport.SpeciesFileColumn;
import org.openforis.collect.manager.speciesimport.SpeciesImportProcess;
import org.openforis.collect.manager.speciesimport.SpeciesImportStatus;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.TaxonDao;
import org.openforis.collect.persistence.TaxonVernacularNameDao;
import org.openforis.collect.persistence.TaxonomyDao;
import org.openforis.idm.metamodel.xml.IdmlParseException;
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
public class SpeciesImportProcessIntegrationTest extends CollectIntegrationTest {

	private static final String VALID_TEST_CSV = "species-test.csv";
	private static final String VALID_EXTRA_COLUMNS_TEST_CSV = "species-valid-extra-columns-test.csv";
	private static final String INVALID_TEST_CSV = "species-invalid-test.csv";
	private static final String INVALID_MISSING_COLUMNS_TEST_CSV = "species-invalid-missing-columns-test.csv";
	
	private static final String TEST_TAXONOMY_NAME = "it_tree";
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private TaxonomyDao taxonomyDao;
	@Autowired
	private TaxonDao taxonDao;
	@Autowired
	private TaxonVernacularNameDao taxonVernacularNameDao;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException {
		survey = loadSurvey();
		surveyManager.importModel(survey);
	}
	
	public SpeciesImportProcess importCSVFile(String fileName) throws Exception {
		File file = getTestFile(fileName);
		CollectTaxonomy taxonomy = new CollectTaxonomy();
		taxonomy.setSurveyId(survey.getId());
		taxonomy.setName(TEST_TAXONOMY_NAME);
		speciesManager.save(taxonomy);
		SpeciesImportProcess process = new SpeciesImportProcess(speciesManager, taxonomy.getId(), file, true);
		process.call();
		return process;
	}
	
	@Test
	public void testSpeciesImport() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_TEST_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		{
			String code = "OLE/CAP/macrocarpa";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(5, code, "Olea capensis subsp. macrocarpa");
			assertEquals(expected, occurrence);
		}
		{
			String code = "OLE/EUR/cuspidata";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(7, code, "Olea europaea subsp. cuspidata");
			assertEquals(expected, occurrence);
		}
		{
			String code = "AFZ/QUA";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(8, code, "Afzelia quanzensis");
			assertEquals(expected, occurrence);
		}
		{
			String code = "ALB/GLA";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(11, code, "Albizia glaberrima");
			assertEquals(expected, occurrence);
		}
		{
			String code = "ALB/SCH/amaniensis";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(12, code, "Albizia schimperiana var. amaniensis");
			assertEquals(expected, occurrence);
		}
	}

	@Test
	public void testSpeciesImportWithExtraColumns() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_EXTRA_COLUMNS_TEST_CSV);
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
		SpeciesImportProcess process = importCSVFile(VALID_TEST_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		int surveyId = survey.getId();
		{
			List<TaxonOccurrence> occurrences = speciesManager.findByVernacularName(surveyId, TEST_TAXONOMY_NAME, "Mbamba", 10);
			assertNotNull(occurrences);
			assertEquals(1, occurrences.size());
			TaxonOccurrence stored = occurrences.get(0);
			TaxonOccurrence expected = new TaxonOccurrence(8, "AFZ/QUA", "Afzelia quanzensis", "Mbambakofi", "swh", null);
			assertEquals(expected, stored);
		}
		{
			List<TaxonOccurrence> occurrences = speciesManager.findByVernacularName(surveyId, TEST_TAXONOMY_NAME, "Mshai-mamba", 10);
			assertNotNull(occurrences);
			assertEquals(1, occurrences.size());
			TaxonOccurrence stored = occurrences.get(0);
			TaxonOccurrence expected = new TaxonOccurrence(10, "ALB/ADI", "Albizia adianthifolia", "Mshai-mamba", "ksb", null);
			assertEquals(expected, stored);
		}
		{
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
		{
			Taxon taxon = findTaxonByCode("BOU/PET");
			assertEquals("Bourreria petiolaris", taxon.getScientificName());
			Integer taxonId = taxon.getSystemId();
			List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(taxonId);
			assertEquals(2, vernacularNames.size());
			assertTrue(contains(vernacularNames, "swh", "Mpanda jongoo"));
			assertTrue(contains(vernacularNames, "lat", "Ehretia petiolaris"));
		}
		{
			Taxon taxon = findTaxonByCode("BOM/RHO");
			assertEquals("Bombax rhodognaphalon", taxon.getScientificName());
			Integer taxonId = taxon.getSystemId();
			List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(taxonId);
			assertEquals(2, vernacularNames.size());
			assertTrue(contains(vernacularNames, "swh", "Msufi mwitu"));
			assertTrue(contains(vernacularNames, "lat", "Rhodognaphalon schumannianum"));
		}
	}

	@Test
	public void testHierarchyImport() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_TEST_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		{
			Taxon variety = findTaxonByCode("ALB/SCH/amaniensis");
			assertNotNull(variety);
			assertEquals(VARIETY, variety.getTaxonRank());
			
			Integer speciesId = variety.getParentId();
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
			assertEquals("ALB", genus.getCode());
			assertEquals(GENUS, genus.getTaxonRank());
			assertEquals("Albizia sp.", genus.getScientificName());
			
			Integer familyId = genus.getParentId();
			assertNotNull(familyId);
			Taxon family = taxonDao.loadById(familyId);
			assertNotNull(family);
			assertNull(family.getParentId());
			assertEquals(FAMILY, family.getTaxonRank());
			assertEquals("Fabaceae", family.getScientificName());
		}
		{
			Taxon subspecies = findTaxonByCode("OLE/EUR/cuspidata");
			assertNotNull(subspecies);
			assertEquals(SUBSPECIES, subspecies.getTaxonRank());
			
			Integer speciesId = subspecies.getParentId();
			assertNotNull(speciesId);
			Taxon species = taxonDao.loadById(speciesId);
			assertNotNull(species);
			assertEquals(SPECIES, species.getTaxonRank());
			assertEquals("OLE/EUR", species.getCode());
			assertEquals("Olea europaea", species.getScientificName());
		}
	}
	
	@Test
	public void testInvalidColumns() throws Exception {
		SpeciesImportProcess process = importCSVFile(INVALID_MISSING_COLUMNS_TEST_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isError());
		List<ParsingError> errors = status.getErrors();
		assertEquals(1, errors.size());
		ParsingError error = errors.get(0);
		ErrorType errorType = error.getErrorType();
		assertEquals(ErrorType.MISSING_REQUIRED_COLUMNS, errorType);
	}
	
	@Test
	public void testErrorHandling() throws Exception {
		SpeciesImportProcess process = importCSVFile(INVALID_TEST_CSV);
		SpeciesImportStatus status = process.getStatus();
		List<ParsingError> errors = status.getErrors();
		assertEquals(8, errors.size());
		
		assertTrue(status.isRowProcessed(1));
		assertTrue(status.isRowProcessed(2));
		assertTrue(status.isRowProcessed(5));
		
		assertFalse(status.isRowProcessed(3));
		assertFalse(status.isRowProcessed(4));
		assertFalse(status.isRowProcessed(6));
		assertFalse(status.isRowProcessed(7));
		assertFalse(status.isRowProcessed(8));
		assertFalse(status.isRowProcessed(9));
		assertFalse(status.isRowProcessed(10));
		assertFalse(status.isRowProcessed(11));
		//unexisting row
		assertFalse(status.isRowProcessed(12));
		
		assertTrue(containsError(errors, 3, SpeciesFileColumn.CODE, ErrorType.EMPTY));
		assertTrue(containsError(errors, 4, SpeciesFileColumn.SCIENTIFIC_NAME, ErrorType.EMPTY));
		assertTrue(containsError(errors, 6, SpeciesFileColumn.CODE, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 7, SpeciesFileColumn.FAMILY, ErrorType.EMPTY));
		assertTrue(containsError(errors, 8, SpeciesFileColumn.NO, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 9, SpeciesFileColumn.SCIENTIFIC_NAME, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 10, SpeciesFileColumn.SCIENTIFIC_NAME, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 10, SpeciesFileColumn.SCIENTIFIC_NAME, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 11, "swh", ErrorType.INVALID_VALUE));
	}
	
	@Test
	public void testExport() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_TEST_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		Taxonomy taxonomy = taxonomyDao.load(survey.getId(), TEST_TAXONOMY_NAME);
		TaxonSummaries summaries = speciesManager.loadFullTaxonSummaries(taxonomy.getId());
		assertNotNull(summaries);
	}

	protected boolean containsError(List<ParsingError> errors, long row, String column, ErrorType type) {
		String[] columnArr = new String[] {column};
		for (ParsingError error : errors) {
			if ( error.getErrorType() == type && error.getRow() == row && Arrays.equals(columnArr, error.getColumns()) ) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean containsError(List<ParsingError> errors, long row, SpeciesFileColumn column, ErrorType type) {
		return containsError(errors, row, column.getColumnName(), type);
	}
	
	protected Taxon findTaxonByCode(String code) {
		Taxonomy taxonomy = taxonomyDao.load(survey.getId(), TEST_TAXONOMY_NAME);
		List<Taxon> results = taxonDao.findByCode(taxonomy.getId(), code, 10);
		assertNotNull(results);
		assertEquals(1, results.size());
		Taxon taxon = results.get(0);
		return taxon;
	}
	
	protected TaxonOccurrence findByCode(String code) {
		List<TaxonOccurrence> occurrences = speciesManager.findByCode(survey.getId(), TEST_TAXONOMY_NAME, code, 10);
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
