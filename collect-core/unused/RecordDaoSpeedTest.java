package org.openforis.collect.persistence;

import java.io.FileInputStream;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordContext;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.ModelSerializer;
import org.openforis.idm.model.TestRecordContext;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=false)
@Transactional
public class RecordDaoSpeedTest {
	private final Log log = LogFactory.getLog(RecordDaoSpeedTest.class);
	
	@Autowired
	protected SurveyDao surveyDao;
	
	@Autowired
	protected RecordDao recordDao;

	@Autowired
	private ExpressionFactory expressionFactory;
	
	@Autowired
	private Validator validator;
//	@Autowired
//	protected SurveyContext recordContext;
	
//	public void testRecordSerialize() throws Exception  {
//		CollectSurvey survey = surveyDao.load("naforma1");
//		CollectRecord r1 = recordDao.load(survey, new CollectRecordContext(),  2099);
//		ModelSerializer ser = new ModelSerializer(10000);
//		FileOutputStream fis = new FileOutputStream("src/it/resources/test-record.bin");
//		try {
//			ser.writeTo(fis, r1.getRootEntity());
//			fis.flush();
//		} finally {
//			fis.close();
//		}
//	}
	
//	public void testRecordStore() throws Exception {
//		CollectSurvey survey = surveyDao.load("naforma1");
//		ModelSerializer ser = new ModelSerializer(10000);
//		FileInputStream fis = new FileInputStream("src/it/resources/test-record.bin");
//		try {
//			CollectRecord r1 = new CollectRecord(new CollectRecordContext(), survey, "2.0");
//			r1.createRootEntity("cluster");
//			r1.setId(2099);
//			ser.mergeFrom(fis, r1.getRootEntity());
//			recordDao.update(r1);
//		} finally {
//			fis.close();
//		}
//		
//	}
	
	@Test
	public void testCRUD() throws Exception  {
//		try {
		// LOAD MODEL
		CollectSurvey survey = surveyDao.load("naforma1");
		CollectRecord r1;
		{
			long t1 = System.currentTimeMillis(); 
			r1 = recordDao.load(survey, new CollectRecordContext(),  2099);
			long t = System.currentTimeMillis() - t1;
			System.out.println("load "+t);
		}
		String s1 = r1.toString();
		{
			long t1 = System.currentTimeMillis();
			recordDao.update(r1);
			long t = System.currentTimeMillis() - t1;
			System.out.println("update "+t);
		}
		{
			long t1 = System.currentTimeMillis(); 
			recordDao.delete(r1);
			long t = System.currentTimeMillis() - t1;
			System.out.println("delete "+t);
		}
		{
			long t1 = System.currentTimeMillis();
			recordDao.insert(r1);
			long t = System.currentTimeMillis() - t1;
			System.out.println("insert "+t);
		}
		CollectRecord r2 = recordDao.load(survey, new CollectRecordContext(),  2099);
		String s2 = r2.toString();
		Assert.assertEquals(s1, s2);
		//		
//		// SAVE NEW
//		CollectRecord record = createTestRecord(survey);
//		recordDao.saveOrUpdate(record);
//		
//		String saved = record.toString();
//		log.debug("Saving record:\n"+saved);
//		
//		// RELOAD
//		record = recordDao.load(survey, record.getId());
//		String reloaded = record.toString();
//		log.debug("Reloaded as:\n"+reloaded);
//		
//		assertEquals(saved, reloaded);
	}
}
