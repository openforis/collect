package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.RecordSummarySortField.Sortable;
import org.openforis.collect.model.SurveySummary;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;
import org.springframework.beans.factory.annotation.Autowired;

import junit.framework.Assert;

public class ModelDaoIntegrationTest extends CollectIntegrationTest {
	private final Log log = LogFactory.getLog(ModelDaoIntegrationTest.class);
	
	@Autowired
	protected RecordDao recordDao;

	private CollectSurvey survey;
	
	@Before
	public void initialize() throws SurveyImportException, IdmlParseException, SurveyValidationException {
		// LOAD MODEL
		survey = surveyManager.get("archenland1");

		if ( survey == null ) {
			// IMPORT MODEL
			survey = importModel();
		}
	}
	
	//@Test
	public void testCRUD() throws Exception  {
		testLoadAllSurveys();

		// SAVE NEW
		CollectRecord saved = createTestRecord(survey, "123_456");
		recordDao.insert(saved);
		
		// RELOAD
		CollectRecord reloaded = recordDao.load(survey, saved.getId(), Step.ENTRY);
		
		assertEquals(saved.getRootEntity(), reloaded.getRootEntity());
	}
	
	@Test
	public void testLoadSummariesByKey() throws Exception  {
		// SAVE NEW
		String testKey = "123_456";
		CollectRecord record = createTestRecord(survey, testKey);
		recordDao.insert(record);
		
		String saved = record.toString();
		log.debug("Saving record:\n"+saved);
		
		// RELOAD
		RecordFilter filter = new RecordFilter(survey, survey.getSchema().getRootEntityDefinition("cluster").getId());
		filter.setKeyValues(Arrays.asList(testKey));
		List<CollectRecordSummary> summaries = recordDao.loadSummaries(filter);
		Assert.assertEquals(1, summaries.size());
		CollectRecordSummary record1 = summaries.get(0);
		String key = record1.getRootEntityKeyValues().get(0);
		assertEquals(key, testKey);
	}

	private void testLoadAllSurveys() {
		List<SurveySummary> list = this.surveyManager.loadCombinedSummaries();
		assertNotNull(list);
	}
	
	@Test
	public void testSurveyNotFoundById() {		
		Survey survey = surveyManager.loadSurvey(-100);
		assertNull(survey);
	}
	
	@Test
	public void testSurveyNotFoundByName() {		
		SurveySummary survey = surveyManager.loadSummaryByName("!!!!!!");
		assertNull(survey);
	}

	private CollectRecord createTestRecord(CollectSurvey survey, String id) {
		CollectRecord record = new CollectRecord(survey, "2.0", "cluster");
		Entity cluster = record.getRootEntity();
		record.setCreationDate(new GregorianCalendar(2011, 11, 31, 23, 59).getTime());
		//record.setCreatedBy("ModelDaoIntegrationTest");
		record.setStep(Step.ENTRY);

		addTestValues(cluster, id);
			
		//set counts
		record.getEntityCounts().add(2);
		
		//set keys
		record.getRootEntityKeyValues().add(id);
		
		return record;
	}

	private void addTestValues(Entity cluster, String id) {
		EntityBuilder.addValue(cluster, "id", new Code(id));
		EntityBuilder.addValue(cluster, "gps_realtime", Boolean.TRUE);
		EntityBuilder.addValue(cluster, "region", new Code("001"));
		EntityBuilder.addValue(cluster, "district", new Code("002"));
		EntityBuilder.addValue(cluster, "crew_no", 10);
		EntityBuilder.addValue(cluster, "map_sheet", "value 1");
		EntityBuilder.addValue(cluster, "map_sheet", "value 2");
		EntityBuilder.addValue(cluster, "vehicle_location", new Coordinate((double)432423423l, (double)4324324l, "srs"));
		EntityBuilder.addValue(cluster, "gps_model", "TomTom 1.232");
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(ts, "date", new Date(2011,2,14));
			EntityBuilder.addValue(ts, "start_time", new Time(8,15));
			EntityBuilder.addValue(ts, "end_time", new Time(15,29));
		}
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(ts, "date", new Date(2011,2,15));
			EntityBuilder.addValue(ts, "start_time", new Time(8,32));
			EntityBuilder.addValue(ts, "end_time", new Time(11,20));
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("1"));
			Entity tree1 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree1, "tree_no", 1);
			EntityBuilder.addValue(tree1, "dbh", 54.2);
			EntityBuilder.addValue(tree1, "total_height", 2.0);
//			EntityBuilder.addValue(tree1, "bole_height", (Double) null).setMetadata(new CollectAttributeMetadata('*',null,"No value specified"));
			RealAttribute boleHeight = EntityBuilder.addValue(tree1, "bole_height", (Double) null);
			boleHeight.getField(0).setSymbol('*');
			boleHeight.getField(0).setRemarks("No value specified");
			Entity tree2 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree2, "tree_no", 2);
			EntityBuilder.addValue(tree2, "dbh", 82.8);
			EntityBuilder.addValue(tree2, "total_height", 3.0);
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("2"));
			Entity tree1 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree1, "tree_no", 1);
			EntityBuilder.addValue(tree1, "dbh", 34.2);
			EntityBuilder.addValue(tree1, "total_height", 2.0);
			Entity tree2 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree2, "tree_no", 2);
			EntityBuilder.addValue(tree2, "dbh", 85.8);
			EntityBuilder.addValue(tree2, "total_height", 4.0);
		}
	}

//	@Test
	public void testLoadRecordSummaries() {
		//get the first root entity
		EntityDefinition rootEntity = survey.getSchema().getFirstRootEntityDefinition();
		String rootEntityName = rootEntity.getName();
		int offset = 0;
		int maxNumberOfRecords = 1;
		RecordSummarySortField sortField = new RecordSummarySortField(Sortable.KEY1);

		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(survey.getSchema().getRootEntityDefinition(rootEntityName).getId());
		filter.setOffset(offset);
		filter.setMaxNumberOfRecords(maxNumberOfRecords);
		List<CollectRecordSummary> list = this.recordDao.loadSummaries(filter, Arrays.asList(sortField));
		assertNotNull(list);
		assertEquals(1, list.size());
		
		CollectRecordSummary summary = list.get(0);
		assertEquals(Step.ENTRY, summary.getStep());
	}
}
