package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;
import org.springframework.beans.factory.annotation.Autowired;

//@RunWith( SpringJUnit4ClassRunner.class )
//@ContextConfiguration( locations = {"classpath:test-context.xml"} )
//@TransactionConfiguration(defaultRollback=false)
//@Transactional
public class RecordDaoTest {
	@SuppressWarnings("unused")
	private final Log log = LogFactory.getLog(RecordDaoTest.class);
	
	@SuppressWarnings("unused")
	private CollectIdmlBindingContext idmlBindingContext;
	
	@Autowired
	protected SurveyDao surveyDao;
	
	@Autowired
	protected RecordDao recordDao;
	
	@Autowired
	protected RecordManager recordManager;
	
	private CollectSurvey survey;
	
	private List<CollectRecord> sampleRecords;
	
	private int numberOfSampleRecords = 1000;
	
//	@Before
	public void beforeTest(){
		surveyDao.loadAll();
		idmlBindingContext = new CollectIdmlBindingContext();
		
		// LOAD MODEL
		survey = surveyDao.load("archenland1");
		
		createAndSaveSampleRecords();
	}
	
//	@After
	public void afterTest() {
		//delete sample records
		for (CollectRecord record : sampleRecords) {
			recordDao.delete(record);
		}
	}
	
	/**
	 * 
	 * Creates sample records with different id and different number of plots.
	 * 
	 */
	private void createAndSaveSampleRecords() {
		sampleRecords = new ArrayList<CollectRecord>();
		
		//create and save clusters
		for (int i = 1; i <= numberOfSampleRecords ; i++) {
			CollectRecord record = createRecord(survey, i);
			sampleRecords.add(record);
			recordDao.saveOrUpdate(record);
		}
	}
	
//	@Test
	public void testLoadRecordSummariesOrderedByClusterId() throws IOException, SurveyImportException, DataInconsistencyException, InvalidIdmlException, NonexistentIdException  {
		EntityDefinition rootEntity = survey.getSchema().getRootEntityDefinitions().get(0);
		String rootEntityName = rootEntity.getName();
		//load record summaries
		int offset = 0;
		int maxNumberOfRecords = 5;
		String orderByFieldName = "key_id";
		String filter = null;
		List<EntityDefinition> countInSummaryListEntityDefinitions = new ArrayList<EntityDefinition>();
		EntityDefinition plotEntity = (EntityDefinition) rootEntity.getChildDefinition("plot");
		countInSummaryListEntityDefinitions.add(plotEntity);
		List<CollectRecord> list = this.recordDao.loadSummaries(survey, recordManager, rootEntityName, offset, maxNumberOfRecords, orderByFieldName, filter);

		assertNotNull(list);
		assertEquals(maxNumberOfRecords, list.size());
		
		//test first record of the page
		CollectRecord sampleRecordSummary;
		List<String> rootEntityKeys;
		
		sampleRecordSummary = list.get(0);
		rootEntityKeys = sampleRecordSummary.getRootEntityKeys();
		
		assertEquals("1", rootEntityKeys.get(0));
		
		//test last record of the page
		sampleRecordSummary = list.get(4);
		rootEntityKeys = sampleRecordSummary.getRootEntityKeys();
		
		assertEquals("5", rootEntityKeys.get(0));
	}

	//@Test
	public void testLoadRecordSummariesOrderedByPlotCount() throws IOException, SurveyImportException, DataInconsistencyException, InvalidIdmlException, NonexistentIdException  {
		EntityDefinition rootEntity = survey.getSchema().getRootEntityDefinitions().get(0);
		String rootEntityName = rootEntity.getName();
		//load record summaries
		int offset = 0;
		int maxNumberOfRecords = 5;
		String orderByFieldName = "count_plot";
		String filter = null;
		
		List<CollectRecord> list = this.recordDao.loadSummaries(survey, recordManager, rootEntityName, offset, maxNumberOfRecords, orderByFieldName, filter);
		
		assertNotNull(list);
		assertEquals(maxNumberOfRecords, list.size());

		//test first record of the page
		{
			CollectRecord sampleRecordSummary = list.get(0);
			List<Integer> entityCounts = sampleRecordSummary.getEntityCounts();
			
			assertEquals(new Integer(1), entityCounts.get(0));
		}
		//test last record of the page
		{
			CollectRecord sampleRecordSummary = list.get(4);
			List<Integer> entityCounts = sampleRecordSummary.getEntityCounts();
		
			assertEquals(new Integer(5), entityCounts.get(0));
		}
	}

	private CollectRecord createRecord(CollectSurvey survey, int sequenceNumber) {
		int skippedCount = new Double(Math.ceil((double) (Math.random() * 40))).intValue();
		int missingCount = new Double(Math.ceil((double) (Math.random() * 50))).intValue();
		int errorsCount = new Double(Math.ceil((double) (Math.random() * 10))).intValue();
		int warningsCount = new Double(Math.ceil((double) (Math.random() * 30))).intValue();
		int numberOfPlots = new Double(Math.ceil((double) (Math.random() * 20))).intValue();
		int numberOfTrees = new Double(Math.ceil((double) (Math.random() * 30))).intValue();;
		
		CollectRecord record = new CollectRecord(recordManager, survey, "2.0");
		record.setCreationDate(new GregorianCalendar(2011, 0, sequenceNumber, 8, 30).getTime());
		//record.setCreatedBy("ModelDaoIntegrationTest");
		record.setStep(Step.ENTRY);
		record.setModifiedDate(new GregorianCalendar(2011, 1, sequenceNumber, 8, 30).getTime());
		record.setSkipped(skippedCount);
		record.setMissing(missingCount);
		record.setErrors(errorsCount);
		record.setWarnings(warningsCount);
		Entity cluster = record.createRootEntity("cluster");
		String keyId = Integer.toString(sequenceNumber);
		EntityBuilder.addValue(cluster, "id", new Code(keyId));
		EntityBuilder.addValue(cluster, "gps_realtime", Boolean.TRUE);
		EntityBuilder.addValue(cluster, "region", new Code("001"));
		EntityBuilder.addValue(cluster, "district", new Code("002"));
		EntityBuilder.addValue(cluster, "vehicle_location", new Coordinate(432423423l, 4324324l,"srs"));
		EntityBuilder.addValue(cluster, "gps_model", "TomTom 1.232");
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			ts.addValue("date", new Date(2011,2,14));
			ts.addValue("start_time", new Time(8,15));
			ts.addValue("end_time", new Time(15,29));
		}
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			ts.addValue("date", new Date(2011,2,15));
			ts.addValue("start_time", new Time(8,32));
			ts.addValue("end_time", new Time(11,20));
		}
		for (int i = 0; i < numberOfPlots; i++) {
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code(Integer.toString(i + 1)));
			
			for (int j = 0; j < numberOfTrees; j++) {
				Entity tree = EntityBuilder.addEntity(plot, "tree");
				tree.addValue("dbh", 54.2);
				tree.addValue("total_height", 2.0);
				
				RealAttribute boleHeight = tree.addValue("bole_height", (Double) null);
				boleHeight.getField().setSymbol('*');
				boleHeight.getField().setRemarks("No value specified");
				//.setMetadata(new CollectAttributeMetadata('*',null,"No value specified"));
			}
		}
		//set counts
		record.getEntityCounts().add(numberOfPlots);
		//set keys
		record.getRootEntityKeys().add(keyId);
		
		return record;
	}

}
