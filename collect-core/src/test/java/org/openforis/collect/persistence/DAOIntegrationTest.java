package org.openforis.collect.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.AlphanumericCode;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.NumericCode;
import org.openforis.idm.model.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=false)
@Transactional
public class DAOIntegrationTest {
	private final Log log = LogFactory.getLog(DAOIntegrationTest.class);
	
	@Autowired
	protected SurveyDAO surveyDao;
	
	@Autowired
	protected RecordDAO recordDao;
	
	@Test
	public void testCRUD() throws IOException, SurveyImportException, DataInconsistencyException  {
		// LOAD MODEL
		Survey survey = surveyDao.load("archenland1");

		if ( survey == null ) {
			// IMPORT MODEL
			survey = importModel();
		}
		
		// SAVE NEW
		CollectRecord record = createRecord(survey);
		recordDao.saveOrUpdate(record);
		
		String saved = record.toString();
		log.debug("Saving record:\n"+saved);
		
		// RELOAD
		record = recordDao.load(survey, record.getId());
		String reloaded = record.toString();
		log.debug("Reloaded as:\n"+reloaded);
		
		assertEquals(saved, reloaded);
		
		// UPDATE
//		updateRecord(record);
		
//		assertEquals(1, cluster.getCount("time_study"));

//		recordDao.saveOrUpdate(record);
	}
	
	@Test
	public void testSurveyNotFoundById() {		
		Survey survey = surveyDao.load(-100);
		assertNull(survey);
	}
	
	@Test
	public void testSurveyNotFoundByName() {		
		Survey survey = surveyDao.load("!!!!!!");
		assertNull(survey);
	}

	private Survey importModel() throws IOException, SurveyImportException {
		URL idm = ClassLoader.getSystemResource("test.idm.xml");
		InputStream is = idm.openStream();
		Survey survey = Survey.unmarshal(is);
		surveyDao.importModel(survey);
		return survey;
	}

	private CollectRecord createRecord(Survey survey) {
		CollectRecord record = new CollectRecord(survey, "cluster", "2.0");
		record.setCreationDate(new GregorianCalendar(2011, 12, 31, 23, 59).getTime());
		record.setCreatedBy("DAOIntegrationTest");

		Entity cluster = record.getRootEntity();
		cluster.addValue("id", new AlphanumericCode("123_456"));
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
		{
			Entity plot = cluster.addEntity("plot");
			plot.addValue("no", new NumericCode(1));
			Entity tree1 = plot.addEntity("tree");
			tree1.addValue("dbh", 54.2);
			tree1.addValue("total_height", 2.0);
			Entity tree2 = plot.addEntity("tree");
			tree2.addValue("dbh", 82.8);
			tree2.addValue("total_height", 3.0);
		}
		{
			Entity plot = cluster.addEntity("plot");
			plot.addValue("no", new NumericCode(2));
			Entity tree1 = plot.addEntity("tree");
			tree1.addValue("dbh", 34.2);
			tree1.addValue("total_height", 2.0);
			Entity tree2 = plot.addEntity("tree");
			tree2.addValue("dbh", 85.8);
			tree2.addValue("total_height", 4.0);
		}
		
		return record;
	}


	private void updateRecord(CollectRecord record) {
		// Update modified date
		record.setModifiedDate(new GregorianCalendar(2012, 1, 1, 0, 1).getTime());
		record.setModifiedBy("DAOIntegrationTest");
		
		// Remove first time_study
		Entity cluster = record.getRootEntity();
		cluster.remove("time_study", 0);
		
		// TODO write update test
	}
}
