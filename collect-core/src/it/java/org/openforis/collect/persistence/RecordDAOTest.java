package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.model.CollectAttributeMetadata;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.RecordSummary;
import org.openforis.collect.model.UIConfiguration.UIConfigurationAdapter;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.BindingContext;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.model.AlphanumericCode;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.NumericCode;
import org.openforis.idm.model.Time;
import org.springframework.beans.factory.annotation.Autowired;

//@RunWith( SpringJUnit4ClassRunner.class )
//@ContextConfiguration( locations = {"classpath:test-context.xml"} )
//@TransactionConfiguration(defaultRollback=false)
//@Transactional
public class RecordDAOTest {
	private final Log log = LogFactory.getLog(RecordDAOTest.class);
	
	private BindingContext bindingContext;
	
	@Autowired
	protected SurveyDAO surveyDao;
	
	@Autowired
	protected RecordDAO recordDao;
	
	@Autowired
	protected RecordSummaryDAO recordSummaryDao;
	
	private Survey survey;
	
	private List<CollectRecord> sampleRecords;
	
	private int numberOfSampleRecords = 1000;
	
//	@Before
	public void beforeTest(){
		surveyDao.loadAll();
		bindingContext = new BindingContext();
		UIConfigurationAdapter configurationAdapter = new UIConfigurationAdapter();
		bindingContext.setConfigurationAdapter(configurationAdapter);
		
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
		EntityDefinition rootEntityDefinition = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition plotDef = (EntityDefinition) rootEntityDefinition.getChildDefinition("plot");
		
		sampleRecords = new ArrayList<CollectRecord>();
		
		//create and save clusters
		for (int i = 1; i <= numberOfSampleRecords ; i++) {
			CollectRecord record = createRecord(survey, i);
			sampleRecords.add(record);
			recordDao.saveOrUpdate(record, Arrays.asList(plotDef));
		}
	}
	
//	@Test
	public void testLoadRecordSummariesOrderedByClusterId() throws IOException, SurveyImportException, DataInconsistencyException, InvalidIdmlException, NonexistentIdException  {
		EntityDefinition rootEntity = survey.getSchema().getRootEntityDefinitions().get(0);

		//load record summaries
		int offset = 0;
		int maxNumberOfRecords = 5;
		String orderByFieldName = "key_id";
		String filter = null;
		List<EntityDefinition> countInSummaryListEntityDefinitions = new ArrayList<EntityDefinition>();
		EntityDefinition plotEntity = (EntityDefinition) rootEntity.getChildDefinition("plot");
		countInSummaryListEntityDefinitions.add(plotEntity);
		List<RecordSummary> list = this.recordSummaryDao.load(rootEntity, countInSummaryListEntityDefinitions, offset, maxNumberOfRecords, orderByFieldName, filter);

		assertNotNull(list);
		assertEquals(maxNumberOfRecords, list.size());
		
		//test first record of the page
		RecordSummary sampleRecordSummary;
		Map<String, String> rootEntityKeys;
		
		sampleRecordSummary = list.get(0);
		rootEntityKeys = sampleRecordSummary.getRootEntityKeys();
		
		assertEquals("1", rootEntityKeys.get("id"));
		
		//test last record of the page
		sampleRecordSummary = list.get(4);
		rootEntityKeys = sampleRecordSummary.getRootEntityKeys();
		
		assertEquals("5", rootEntityKeys.get("id"));
	}

	//@Test
	public void testLoadRecordSummariesOrderedByPlotCount() throws IOException, SurveyImportException, DataInconsistencyException, InvalidIdmlException, NonexistentIdException  {
		EntityDefinition rootEntity = survey.getSchema().getRootEntityDefinitions().get(0);

		//load record summaries
		int offset = 0;
		int maxNumberOfRecords = 5;
		String orderByFieldName = "count_plot";
		String filter = null;
		
		List<EntityDefinition> countInSummaryListEntityDefinitions = new ArrayList<EntityDefinition>();
		EntityDefinition plotEntity = (EntityDefinition) rootEntity.getChildDefinition("plot");
		countInSummaryListEntityDefinitions.add(plotEntity);
		List<RecordSummary> list = this.recordSummaryDao.load(rootEntity, countInSummaryListEntityDefinitions, offset, maxNumberOfRecords, orderByFieldName, filter);
		
		assertNotNull(list);
		assertEquals(maxNumberOfRecords, list.size());

		//test first record of the page
		{
			RecordSummary sampleRecordSummary = list.get(0);
			Map<String, Integer> entityCounts = sampleRecordSummary.getEntityCounts();
			
			assertEquals(new Integer(1), entityCounts.get("plot"));
		}
		//test last record of the page
		{
			RecordSummary sampleRecordSummary = list.get(4);
			Map<String, Integer> entityCounts = sampleRecordSummary.getEntityCounts();
		
			assertEquals(new Integer(5), entityCounts.get("plot"));
		}
	}

	private CollectRecord createRecord(Survey survey, int sequenceNumber) {
		int skippedCount = new Double(Math.ceil((double) (Math.random() * 40))).intValue();
		int missingCount = new Double(Math.ceil((double) (Math.random() * 50))).intValue();
		int errorsCount = new Double(Math.ceil((double) (Math.random() * 10))).intValue();
		int warningsCount = new Double(Math.ceil((double) (Math.random() * 30))).intValue();
		int numberOfPlots = new Double(Math.ceil((double) (Math.random() * 20))).intValue();
		int numberOfTrees = new Double(Math.ceil((double) (Math.random() * 30))).intValue();;
		
		CollectRecord record = new CollectRecord(survey, "cluster", "2.0");
		record.setCreationDate(new GregorianCalendar(2011, 0, sequenceNumber, 8, 30).getTime());
		//record.setCreatedBy("DAOIntegrationTest");
		record.setStep(Step.ENTRY);
		record.setModifiedDate(new GregorianCalendar(2011, 1, sequenceNumber, 8, 30).getTime());
		record.setSkipped(skippedCount);
		record.setMissing(missingCount);
		record.setErrors(errorsCount);
		record.setWarnings(warningsCount);
		Entity cluster = record.getRootEntity();
		String keyId = Integer.toString(sequenceNumber);
		cluster.addValue("id", new AlphanumericCode(keyId));
		cluster.addValue("gps_realtime", Boolean.TRUE);
		cluster.addValue("region", new NumericCode(001));
		cluster.addValue("district", new NumericCode(002));
		cluster.addValue("vehicle_location", new Coordinate(432423423l, 4324324l,"srs"));
		cluster.addValue("gps_model", "TomTom 1.232");
		{
			Entity ts = cluster.addEntity("time_study");
			ts.addValue("date", new Date(2011,2,14));
			ts.addValue("start_time", new Time(8,15));
			ts.addValue("end_time", new Time(15,29));
		}
		{
			Entity ts = cluster.addEntity("time_study");
			ts.addValue("date", new Date(2011,2,15));
			ts.addValue("start_time", new Time(8,32));
			ts.addValue("end_time", new Time(11,20));
		}
		for (int i = 0; i < numberOfPlots; i++) {
			Entity plot = cluster.addEntity("plot");
			plot.addValue("no", new AlphanumericCode(Integer.toString(i + 1)));
			
			for (int j = 0; j < numberOfTrees; j++) {
				Entity tree = plot.addEntity("tree");
				tree.addValue("dbh", 54.2);
				tree.addValue("total_height", 2.0);
				tree.addValue("bole_height", (Double) null).setMetadata(new CollectAttributeMetadata('*',null,"No value specified"));
			}
		}
		//set counts
		EntityDefinition plotDef = (EntityDefinition) cluster.getDefinition().getChildDefinition("plot");
		record.getCounts().put(plotDef.getPath(), numberOfPlots);
		//set keys
		NodeDefinition idDef = cluster.getDefinition().getChildDefinition("id");
		record.getKeys().put(idDef.getPath(), keyId);
		
		return record;
	}

}
