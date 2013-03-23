package org.openforis.collect.manager;

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
import org.junit.runner.RunWith;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.samplingdesignimport.SamplingDesignFileColumn;
import org.openforis.collect.manager.samplingdesignimport.SamplingDesignImportProcess;
import org.openforis.collect.manager.samplingdesignimport.SamplingDesignImportStatus;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.xml.IdmlParseException;
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
public class SamplingDesignImportProcessIntegrationTest extends CollectIntegrationTest {

	private static final String VALID_TEST_CSV = "sampling-design-test.csv";
	private static final String VALID_FLAT_TEST_CSV = "sampling-design-flat-test.csv";
	private static final String INVALID_TEST_CSV = "sampling-design-invalid-test.csv";

	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SurveyManager surveyManager;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException {
		survey = loadSurvey();
		surveyManager.saveSurveyWork(survey);
	}
	
	public SamplingDesignImportProcess importCSVFile(String fileName) throws Exception {
		File file = getTestFile(fileName);
		SamplingDesignImportProcess process = new SamplingDesignImportProcess(samplingDesignManager, survey, true, file, true);
		process.call();
		return process;
	}
	
	@Test
	public void testImport() throws Exception {
		SamplingDesignImportProcess process = importCSVFile(VALID_TEST_CSV);
		SamplingDesignImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		assertEquals(27, status.getProcessed());
		
		SamplingDesignSummaries samplingDesignSummaries = samplingDesignManager.loadBySurveyWork(survey.getId(), 0, 30);
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
		SamplingDesignImportProcess process = importCSVFile(VALID_FLAT_TEST_CSV);
		SamplingDesignImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		
		SamplingDesignSummaries samplingDesignSummaries = samplingDesignManager.loadBySurveyWork(survey.getId(), 0, 30);
		assertNotNull(samplingDesignSummaries);
		assertEquals(5, samplingDesignSummaries.getTotalCount());
		
		List<SamplingDesignItem> items = samplingDesignSummaries.getRecords();
		assertNotNull(findItem(items, -10000d, 100000d, "1_01"));
		assertNotNull(findItem(items, 200000d, -2000000d, "1_02"));
		assertNotNull(findItem(items, 806340d, 9320050d, "10_114"));
		assertNotNull(findItem(items, 806680d, 9305020d, "10_117"));
	}
	
	@Test
	public void testInvalidData() throws Exception {
		SamplingDesignImportProcess process = importCSVFile(INVALID_TEST_CSV);
		SamplingDesignImportStatus status = process.getStatus();
		assertTrue(status.isError());
		List<ParsingError> errors = status.getErrors();
		assertEquals(10, errors.size());
		
		assertTrue(containsError(errors, 3, SamplingDesignFileColumn.LEVEL_2, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 4, SamplingDesignFileColumn.LEVEL_2, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 11, SamplingDesignFileColumn.LOCATION_COLUMNS, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 13, SamplingDesignFileColumn.LOCATION_COLUMNS, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 14, SamplingDesignFileColumn.LOCATION_COLUMNS, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 17, SamplingDesignFileColumn.LEVEL_2, ErrorType.EMPTY));
		assertTrue(containsError(errors, 20, SamplingDesignFileColumn.LEVEL_1, ErrorType.EMPTY));
		assertTrue(containsError(errors, 21, SamplingDesignFileColumn.LOCATION_COLUMNS, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 25, SamplingDesignFileColumn.X, ErrorType.EMPTY));
		assertTrue(containsError(errors, 26, SamplingDesignFileColumn.Y, ErrorType.EMPTY));
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
