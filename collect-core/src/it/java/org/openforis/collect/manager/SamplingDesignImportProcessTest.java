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
import org.openforis.collect.manager.referenceDataImport.ParsingError;
import org.openforis.collect.manager.referenceDataImport.ParsingError.ErrorType;
import org.openforis.collect.manager.samplingDesignImport.SamplingDesignFileColumn;
import org.openforis.collect.manager.samplingDesignImport.SamplingDesignImportProcess;
import org.openforis.collect.manager.samplingDesignImport.SamplingDesignImportStatus;
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
public class SamplingDesignImportProcessTest extends CollectIntegrationTest {

	private static final String VALID_TEST_CSV = "test-sampling-design.csv";
	private static final String INVALID_TEST_CSV = "test-invalid-sampling-design.csv";

	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SurveyManager surveyManager;
	
	private Integer surveyWorkId;
	
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException {
		CollectSurvey survey = loadSurvey();
		surveyManager.saveSurveyWork(survey);
		surveyWorkId = survey.getId();
	}
	
	public SamplingDesignImportProcess importCSVFile(String fileName) throws Exception {
		File file = getTestFile(fileName);
		SamplingDesignImportProcess process = new SamplingDesignImportProcess(samplingDesignManager, surveyWorkId, true, file, true);
		process.call();
		return process;
	}
	
	@Test
	public void testImport() throws Exception {
		SamplingDesignImportProcess process = importCSVFile(VALID_TEST_CSV);
		SamplingDesignImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		assertEquals(23, status.getProcessed());
		
		SamplingDesignSummaries samplingDesignSummaries = samplingDesignManager.loadBySurveyWork(surveyWorkId, 0, 30);
		assertNotNull(samplingDesignSummaries);
		assertEquals(22, samplingDesignSummaries.getTotalCount());
		
		List<SamplingDesignItem> items = samplingDesignSummaries.getRecords();
		assertNotNull(findItem(items, "SRID=EPSG:21035;POINT(806090 9320050)", "10_114", "7"));
		assertNotNull(findItem(items, "SRID=EPSG:21035;POINT(805680 9305020)", "10_117", "6"));
	}
	
	@Test
	public void testInvalidData() throws Exception {
		SamplingDesignImportProcess process = importCSVFile(INVALID_TEST_CSV);
		SamplingDesignImportStatus status = process.getStatus();
		assertTrue(status.isError());
		List<ParsingError> errors = status.getErrors();
		assertEquals(8, errors.size());
		
		assertTrue(containsError(errors, 3, SamplingDesignFileColumn.LEVEL_2, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 4, SamplingDesignFileColumn.LEVEL_2, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 11, SamplingDesignFileColumn.LOCATION, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 13, SamplingDesignFileColumn.LOCATION, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 14, SamplingDesignFileColumn.LOCATION, ErrorType.DUPLICATE_VALUE));
		assertTrue(containsError(errors, 17, SamplingDesignFileColumn.LEVEL_2, ErrorType.EMPTY));
		assertTrue(containsError(errors, 20, SamplingDesignFileColumn.LEVEL_1, ErrorType.EMPTY));
		assertTrue(containsError(errors, 21, SamplingDesignFileColumn.LOCATION, ErrorType.DUPLICATE_VALUE));
	}

	protected boolean containsError(List<ParsingError> errors, long row, SamplingDesignFileColumn column, ErrorType type) {
		for (ParsingError error : errors) {
			if ( error.getErrorType() == type && error.getRow() == row && error.getColumn().equals(column.getName())) {
				return true;
			}
		}
		return false;
	}
	
	protected SamplingDesignItem findItem(List<SamplingDesignItem> items, String location,
			String... levelCodes) {
		List<String> levelCodesList = Arrays.asList(levelCodes);
		for (SamplingDesignItem item : items) {
			if ( item.getLevelCodes().equals(levelCodesList) && item.getLocation().equals(location) ) {
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
