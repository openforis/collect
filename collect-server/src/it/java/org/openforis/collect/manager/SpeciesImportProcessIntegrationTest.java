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
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.metadata.species.SpeciesFileColumn;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.openforis.collect.manager.exception.SurveyValidationException;
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
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
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
	
	@SuppressWarnings("deprecation")
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException, SurveyValidationException {
		survey = loadSurvey();
		surveyManager.importModel(survey);
	}
	
	public SpeciesImportProcess importCSVFile(String fileName) throws Exception {
		File file = getTestFile(fileName);
		CollectTaxonomy taxonomy = new CollectTaxonomy();
		taxonomy.setSurvey(survey);
		taxonomy.setName(TEST_TAXONOMY_NAME);
		speciesManager.save(taxonomy);
		SpeciesImportProcess process = new SpeciesImportProcess(surveyManager, speciesManager, survey, 
				taxonomy.getId(), file, new CSVFileOptions(), true);
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
			expected.setTaxonRank(TaxonRank.SUBSPECIES);
			assertEquals(expected, occurrence);
		}
		{
			String code = "OLE/EUR/cuspidata";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(7, code, "Olea europaea subsp. cuspidata");
			expected.setTaxonRank(TaxonRank.SUBSPECIES);
			assertEquals(expected, occurrence);
		}
		{
			String code = "AFZ/QUA";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(8, code, "Afzelia quanzensis");
			expected.setTaxonRank(TaxonRank.SPECIES);
			assertEquals(expected, occurrence);
		}
		{
			String code = "ALB/GLA";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(11, code, "Albizia glaberrima");
			expected.setTaxonRank(TaxonRank.SPECIES);
			assertEquals(expected, occurrence);
		}
		{
			String code = "ALB/SCH/amaniensis";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(12, code, "Albizia schimperiana var. amaniensis");
			expected.setTaxonRank(TaxonRank.VARIETY);
			assertEquals(expected, occurrence);
		}
		{
			String code = "RUT/CIT/RETxPAR";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(15, code, "Citrus reticulata x Citrus paradisi");
			expected.setTaxonRank(TaxonRank.SPECIES);
			assertEquals(expected, occurrence);
		}
		{
			String code = "IRI/IRI/GER";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(16, code, "Iris ×germanica");
			expected.setTaxonRank(TaxonRank.SPECIES);
			assertEquals(expected, occurrence);
		}
		{
			String code = "IRI/IRI/BUI";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(17, code, "Iris x buiana");
			expected.setTaxonRank(TaxonRank.SPECIES);
			assertEquals(expected, occurrence);
		}
		{
			String code = "ACH/MIL/CER";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(18, code, "Achillea millefolium 'Cerise Queen'");
			expected.setTaxonRank(TaxonRank.CULTIVAR);
			assertEquals(expected, occurrence);
		}
	}

	@Test
	public void testSpeciesImportWithExtraColumns() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_EXTRA_COLUMNS_TEST_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		{
			String code = "AFZ/QUA";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(8, code, "Afzelia quanzensis");
			expected.setInfoAttributes(Arrays.asList("TEST 3"));
			expected.setTaxonRank(SPECIES);
			assertEquals(expected, occurrence);
		}
		{
			String code = "ALB/GLA";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(11, code, "Albizia glaberrima");
			expected.setInfoAttributes(Arrays.asList((String) null));
			expected.setTaxonRank(SPECIES);
			assertEquals(expected, occurrence);
		}
		{
			String code = "ALB/SCH/amaniensis";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(12, code, "Albizia schimperiana var. amaniensis");
			expected.setInfoAttributes(Arrays.asList("TEST 1"));
			expected.setTaxonRank(VARIETY);
			assertEquals(expected, occurrence);
		}
	}
	
	@Test
	public void testVernacularNamesImport() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_TEST_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		CollectTaxonomy taxonomy = speciesManager.loadTaxonomyByName(survey, TEST_TAXONOMY_NAME);
		TaxonSearchParameters taxonSearchParameters = new TaxonSearchParameters();
		taxonSearchParameters.setHighestRank(FAMILY);
		{
			List<TaxonOccurrence> occurrences = speciesManager.findByVernacularName(taxonomy, null, "Mbamba", 10, taxonSearchParameters);
			assertNotNull(occurrences);
			assertEquals(1, occurrences.size());
			TaxonOccurrence stored = occurrences.get(0);
			TaxonOccurrence expected = new TaxonOccurrence(8, "AFZ/QUA", "Afzelia quanzensis", "Mbambakofi", "swh", null);
			expected.setTaxonRank(SPECIES);
			assertEquals(expected, stored);
		}
		{
			List<TaxonOccurrence> occurrences = speciesManager.findByVernacularName(taxonomy, null, "Mshai-mamba", 10, taxonSearchParameters);
			assertNotNull(occurrences);
			assertEquals(1, occurrences.size());
			TaxonOccurrence stored = occurrences.get(0);
			TaxonOccurrence expected = new TaxonOccurrence(10, "ALB/ADI", "Albizia adianthifolia", "Mshai-mamba", "ksb", null);
			expected.setTaxonRank(SPECIES);
			assertEquals(expected, stored);
		}
		{
			Taxon taxon = findTaxonByCode("ALB/ADI");
			Long taxonId = taxon.getSystemId();
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
			Long taxonId = taxon.getSystemId();
			List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(taxonId);
			assertEquals(1, vernacularNames.size());
			assertTrue(contains(vernacularNames, "swh", "Mpanda jongoo"));
		}
		{
			Taxon taxon = findTaxonByCode("BOM/RHO");
			assertEquals("Bombax rhodognaphalon", taxon.getScientificName());
			Long taxonId = taxon.getSystemId();
			List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(taxonId);
			assertEquals(1, vernacularNames.size());
			assertTrue(contains(vernacularNames, "swh", "Msufi mwitu"));
		}
	}

	@Test
	public void testHierarchyImport() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_TEST_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		CollectTaxonomy taxonomy = taxonomyDao.loadByName(survey, TEST_TAXONOMY_NAME);
		{
			Taxon variety = findTaxonByCode("ALB/SCH/amaniensis");
			assertNotNull(variety);
			assertEquals(VARIETY, variety.getTaxonRank());
			
			Long speciesId = variety.getParentId();
			assertNotNull(speciesId);
			Taxon species = taxonDao.loadById(taxonomy, speciesId);
			assertNotNull(species);
			assertNull(species.getCode());
			assertEquals(SPECIES, species.getTaxonRank());
			assertEquals("Albizia schimperiana", species.getScientificName());
			
			Long genusId = species.getParentId();
			assertNotNull(genusId);
			Taxon genus = taxonDao.loadById(taxonomy, genusId);
			assertNotNull(genus);
			assertEquals("ALB", genus.getCode());
			assertEquals(GENUS, genus.getTaxonRank());
			assertEquals("Albizia sp.", genus.getScientificName());
			
			Long familyId = genus.getParentId();
			assertNotNull(familyId);
			Taxon family = taxonDao.loadById(taxonomy, familyId);
			assertNotNull(family);
			assertNull(family.getParentId());
			assertEquals(FAMILY, family.getTaxonRank());
			assertEquals("Fabaceae", family.getScientificName());
		}
		{
			Taxon subspecies = findTaxonByCode("OLE/EUR/cuspidata");
			assertNotNull(subspecies);
			assertEquals(SUBSPECIES, subspecies.getTaxonRank());
			
			Long speciesId = subspecies.getParentId();
			assertNotNull(speciesId);
			Taxon species = taxonDao.loadById(taxonomy, speciesId);
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
		assertEquals(7, errors.size());
		
		assertTrue(status.isRowProcessed(1));
		assertTrue(status.isRowProcessed(2));
		assertTrue(status.isRowProcessed(4));
		assertTrue(status.isRowProcessed(5));
		
		assertFalse(status.isRowProcessed(3));
		assertFalse(status.isRowProcessed(6));
		assertFalse(status.isRowProcessed(7));
		assertFalse(status.isRowProcessed(8));
		assertFalse(status.isRowProcessed(9));
		assertFalse(status.isRowProcessed(10));
		assertFalse(status.isRowProcessed(11));
		//unexisting row
		assertFalse(status.isRowProcessed(12));
		
		assertTrue(containsError(errors, 3, SpeciesFileColumn.CODE, ErrorType.EMPTY));
		assertTrue(containsError(errors, 6, SpeciesFileColumn.CODE, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 7, SpeciesFileColumn.FAMILY, ErrorType.EMPTY));
		assertTrue(containsError(errors, 8, SpeciesFileColumn.NO, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 9, SpeciesFileColumn.SCIENTIFIC_NAME, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 10, SpeciesFileColumn.SCIENTIFIC_NAME, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 11, "swh", ErrorType.INVALID_VALUE));
	}
	
	@Test
	public void testExport() throws Exception {
		SpeciesImportProcess process = importCSVFile(VALID_TEST_CSV);
		SpeciesImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		CollectTaxonomy taxonomy = taxonomyDao.loadByName(survey, TEST_TAXONOMY_NAME);
		TaxonSummaries summaries = speciesManager.loadFullTaxonSummariesOld(taxonomy);
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
		CollectTaxonomy taxonomy = taxonomyDao.loadByName(survey, TEST_TAXONOMY_NAME);
		List<Taxon> results = taxonDao.findByCode(taxonomy, FAMILY, code, 10);
		assertNotNull(results);
		assertEquals(1, results.size());
		Taxon taxon = results.get(0);
		return taxon;
	}
	
	protected TaxonOccurrence findByCode(String code) {
		CollectTaxonomy taxonomy = taxonomyDao.loadByName(survey, TEST_TAXONOMY_NAME);
		TaxonSearchParameters taxonSearchParameters = new TaxonSearchParameters();
		List<TaxonOccurrence> occurrences = speciesManager.findByCode(taxonomy, code, 10, taxonSearchParameters);
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
