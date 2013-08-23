package org.openforis.collect.manager.dataimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ReferenceDataImportStatus;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
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
public class CSVDataImportProcessIntegrationTest extends CollectIntegrationTest {

	private static final String VALID_TEST_CSV = "data-import-test.csv";

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordDao recordDao;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException {
		survey = loadSurvey();
		surveyManager.saveSurveyWork(survey);
	}
	
	public CSVDataImportProcess importCSVFile(String fileName, int parentEntityDefinitionId) throws Exception {
		File file = getTestFile(fileName);
		CSVDataImportProcess process = new CSVDataImportProcess(recordDao, file, survey, parentEntityDefinitionId, true);
		process.call();
		return process;
	}
	
	@Test
	public void testImport() throws Exception {
		CollectRecord record = createTestRecord(survey, "10_114");
		recordDao.insert(record);
		Entity cluster = record.getRootEntity();
		EntityDefinition clusterDefn = cluster.getDefinition();
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		CSVDataImportProcess process = importCSVFile(VALID_TEST_CSV, plotDefn.getId());
		ReferenceDataImportStatus<ParsingError> status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		assertEquals(3, status.getProcessed());
		
//		Entity plot1 = (Entity) cluster.get("plot", 0);
	}
	
	protected boolean containsError(List<ParsingError> errors, long row,
			String column) {
		for (ParsingError error : errors) {
			if ( error.getRow() == row && Arrays.asList(error.getColumns()).contains(column) ) {
				return true;
			}
		}
		return false;
	}

	private CollectRecord createTestRecord(CollectSurvey survey, String id) {
		CollectRecord record = new CollectRecord(survey, "2.0");
		Entity cluster = record.createRootEntity("cluster");
		record.setCreationDate(new GregorianCalendar(2011, 11, 31, 23, 59).getTime());
		record.setStep(Step.ENTRY);
		EntityBuilder.addValue(cluster, "id", new Code(id));
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("1"));
			EntityBuilder.addValue(plot, "subplot", "A");
			EntityBuilder.addValue(plot, "land_use", new Code("1"));
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("2"));
			EntityBuilder.addValue(plot, "subplot", "A");
			EntityBuilder.addValue(plot, "land_use", new Code("2"));
		}
		record.updateRootEntityKeyValues();
		record.updateEntityCounts();
		return record;
	}

	protected File getTestFile(String fileName) throws URISyntaxException {
		URL fileUrl = ClassLoader.getSystemResource(fileName);
		File file = new File(fileUrl.toURI());
		return file;
	}
}
