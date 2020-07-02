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
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignFileColumn;
import org.openforis.collect.io.metadata.samplingdesign.SamplingPointDataImportJob;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.concurrency.JobManager;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
public class SamplingPointDataImportJobIntegrationTest extends CollectIntegrationTest {

	private static final String VALID_TEST_CSV = "sampling-design-test.csv";
	private static final String VALID_FLAT_TEST_CSV = "sampling-design-flat-test.csv";
	private static final String INVALID_TEST_CSV = "sampling-design-invalid-test.csv";

	@Autowired
	private JobManager jobManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SurveyManager surveyManager;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws IdmlParseException, IOException, SurveyStoreException, SurveyValidationException {
		survey = loadSurvey();
		survey.setTemporary(true);
		surveyManager.save(survey);
	}
	
	public SamplingPointDataImportJob importCSVFile(String fileName) throws Exception {
		File file = getTestFile(fileName);
		SamplingPointDataImportJob job = jobManager.createJob(SamplingPointDataImportJob.class);
		job.setSurvey(survey);
		job.setFile(file);
		jobManager.start(job, false);
		return job;
	}
	
	@Test
	public void testImport() throws Exception {
		SamplingPointDataImportJob job = importCSVFile(VALID_TEST_CSV);
		assertTrue(job.isCompleted());
		assertTrue(job.getSkippedRows().isEmpty());
		assertEquals(27, job.getProcessedItems());
		
		SamplingDesignSummaries samplingDesignSummaries = samplingDesignManager.loadBySurvey(survey.getId(), 0, 30);
		assertNotNull(samplingDesignSummaries);
		assertEquals(26, samplingDesignSummaries.getTotalCount());
		
		List<SamplingDesignItem> items = samplingDesignSummaries.getRecords();
		assertNotNull(findItem(items, -10000d, 100000d, "1_01", "1"));
		assertNotNull(findItem(items, 200000d, -2000000d, "1_02", "1"));
		assertNotNull(findItem(items, 806090d, 9320050d, "10_114", "7"));
		assertNotNull(findItem(items, 805680d, 9305020d, "10_117", "6"));
	}
	
	@Test
	public void testFlatImport() throws Exception {
		SamplingPointDataImportJob job = importCSVFile(VALID_FLAT_TEST_CSV);
		assertTrue(job.isCompleted());
		assertTrue(job.getSkippedRows().isEmpty());
		
		SamplingDesignSummaries samplingDesignSummaries = samplingDesignManager.loadBySurvey(survey.getId(), 0, 30);
		assertNotNull(samplingDesignSummaries);
		assertEquals(6, samplingDesignSummaries.getTotalCount());
		
		List<SamplingDesignItem> items = samplingDesignSummaries.getRecords();
		{
			SamplingDesignItem item = findItem(items, -10000d, 100000d, "1_01");
			assertNotNull(item);
			assertEquals(Arrays.asList("001", "002"), item.getInfoAttributes().subList(0, 2));
		}
		assertNotNull(findItem(items, 200000d, -2000000d, "1_02"));
		assertNotNull(findItem(items, 806340d, 9320050d, "10_114"));
		assertNotNull(findItem(items, 806680d, 9305020d, "10_117"));
		assertNotNull(findItem(items, 80.1234d, -6.908d, "10_115"));
	}
	
	@Test
	public void testInvalidData() throws Exception {
		SamplingPointDataImportJob job = importCSVFile(INVALID_TEST_CSV);
		assertTrue(job.isFailed());
		List<ParsingError> errors = job.getErrors();
		assertEquals(6, errors.size());
		
		assertTrue(containsError(errors, 3, SamplingDesignFileColumn.LEVEL_2, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 4, SamplingDesignFileColumn.LEVEL_2, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 14, SamplingDesignFileColumn.LEVEL_2, ErrorType.EMPTY));
		assertTrue(containsError(errors, 17, SamplingDesignFileColumn.LEVEL_1, ErrorType.EMPTY));
		assertTrue(containsError(errors, 22, SamplingDesignFileColumn.X, ErrorType.EMPTY));
		assertTrue(containsError(errors, 23, SamplingDesignFileColumn.Y, ErrorType.EMPTY));
	}

	protected boolean containsError(List<ParsingError> errors, long row, SamplingDesignFileColumn column, ErrorType type) {
		return containsError(errors, row, new SamplingDesignFileColumn[] {column}, type);
	}

	protected boolean containsError(List<ParsingError> errors, long row, SamplingDesignFileColumn[] columns, ErrorType type) {
		String[] colNames = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			SamplingDesignFileColumn col = columns[i];
			colNames[i] = col.getColumnName();
		}
		for (ParsingError error : errors) {
			if ( error.getErrorType() == type && error.getRow() == row && Arrays.equals(colNames, error.getColumns())) {
				return true;
			}
		}
		return false;
	}
	
	protected SamplingDesignItem findItem(List<SamplingDesignItem> items, Double x, Double y,
			String... levelCodes) {
		List<String> levelCodesList = Arrays.asList(levelCodes);
		for (SamplingDesignItem item : items) {
			if ( item.getLevelCodes().equals(levelCodesList) && x.equals(item.getX()) && y.equals(item.getY()) ) {
				return item;
			}
		}
		return null;
	}

	protected File getTestFile(String fileName) throws URISyntaxException {
		URL fileUrl = ClassLoader.getSystemResource(fileName);
		File file = new File(fileUrl.toURI());
		return file;
	}
}
