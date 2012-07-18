package org.openforis.collect.manager;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.manager.RecordDataIndexManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.expression.ExpressionFactory;
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
public class RecordDataIndexManagerIntegrationTest {
	//private final Log log = LogFactory.getLog(ConfigurationDaoIntegrationTest.class);
	
	@Autowired
	protected RecordDataIndexManager indexManager;
	
	@Test
	public void roundTripTest() throws Exception  {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		indexManager.index(record);
		NodeDefinition autoCompleteNodeDefn = survey.getSchema().getByPath("/cluster/gps_model");
		Set<String> result = indexManager.search(survey, autoCompleteNodeDefn.getId(), 0, "GPS");
		assertNotNull(result);
	}
	
	private CollectSurvey loadSurvey() throws IOException, SurveyImportException, InvalidIdmlException {
		URL idm = ClassLoader.getSystemResource("test.idm.xml");
		InputStream is = idm.openStream();
		CollectSurveyContext context = new CollectSurveyContext(new ExpressionFactory(), null, null);
		CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext(context);
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("archenland1");
		return survey;
	}

	private CollectRecord createTestRecord(CollectSurvey survey) {
		CollectRecord record = new CollectRecord(survey, "2.0");
		Entity cluster = record.createRootEntity("cluster");
		record.setCreationDate(new GregorianCalendar(2011, 12, 31, 23, 59).getTime());
		record.setStep(Step.ENTRY);
		cluster.addValue("id", new Code("123_456"));
		cluster.addValue("gps_model", "GPS Model 1");
		return record;
	}

}
