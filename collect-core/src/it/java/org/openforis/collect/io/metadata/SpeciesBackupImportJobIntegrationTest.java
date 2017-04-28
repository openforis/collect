package org.openforis.collect.io.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.metadata.species.SpeciesBackupFileColumn;
import org.openforis.collect.io.metadata.species.SpeciesBackupImportJob;
import org.openforis.collect.io.metadata.species.SpeciesBackupImportTask;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.TaxonDao;
import org.openforis.collect.persistence.TaxonomyDao;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
public class SpeciesBackupImportJobIntegrationTest extends CollectIntegrationTest {

	private static final String VALID_TEST_CSV = "species-backup-test.csv";
//	private static final String VALID_EXTRA_COLUMNS_TEST_CSV = "species-valid-extra-columns-test.csv";
//	private static final String INVALID_TEST_CSV = "species-invalid-test.csv";
//	private static final String INVALID_MISSING_COLUMNS_TEST_CSV = "species-invalid-missing-columns-test.csv";
	
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
	private CollectJobManager jobManager;
	
	private CollectSurvey survey;
	
	@SuppressWarnings("deprecation")
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException, SurveyValidationException {
		survey = loadSurvey();
		survey.setTemporary(false);
		surveyManager.importModel(survey);
	}
	
	private SpeciesBackupImportJob importCSVFile(String fileName) throws Exception {
		File file = getTestFile(fileName);
		CollectTaxonomy taxonomy = new CollectTaxonomy();
		taxonomy.setSurveyId(survey.getId());
		taxonomy.setName(TEST_TAXONOMY_NAME);
		speciesManager.save(taxonomy);
		SpeciesBackupImportJob job = jobManager.createJob(SpeciesBackupImportJob.class);
		job.setFile(file);
		job.setSurvey(survey);
		job.setTaxonomyName(TEST_TAXONOMY_NAME);
		jobManager.start(job, false);
		return job;
	}
	
	@Test
	public void testSpeciesImport() throws Exception {
		SpeciesBackupImportJob job = importCSVFile(VALID_TEST_CSV);
		assertTrue(job.isCompleted());
		SpeciesBackupImportTask task = (SpeciesBackupImportTask) job.getTasks().get(0);
		assertTrue(task.getSkippedRows().isEmpty());
		{
			String code = "OLE/CAP/macrocarpa";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(code, "Olea capensis ssp. macrocarpa");
			assertEquals(expected, occurrence);
		}
		{
			String code = "ALB/ADI";
			TaxonOccurrence occurrence = findByCode(code);
			TaxonOccurrence expected = new TaxonOccurrence(code, "Albizia adianthifolia");
			assertEquals(expected, occurrence);
		}
	}
	
	@Test
	public void testExport() throws Exception {
		SpeciesBackupImportJob job = importCSVFile(VALID_TEST_CSV);
		assertTrue(job.isCompleted());
		Taxonomy taxonomy = taxonomyDao.loadByName(survey.getId(), TEST_TAXONOMY_NAME);
		TaxonSummaries summaries = speciesManager.loadFullTaxonSummariesOld(survey, taxonomy.getId());
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
	
	protected boolean containsError(List<ParsingError> errors, long row, SpeciesBackupFileColumn column, ErrorType type) {
		return containsError(errors, row, column.getColumnName(), type);
	}
	
	protected Taxon findTaxonByCode(String code) {
		Taxonomy taxonomy = taxonomyDao.loadByName(survey.getId(), TEST_TAXONOMY_NAME);
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
