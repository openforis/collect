package org.openforis.collect;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.runner.RunWith;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public abstract class CollectIntegrationTest {

	@Autowired
	protected CollectSurveyContext collectSurveyContext;
	@Autowired
	protected SurveyDao surveyDao;
	@Autowired
	protected SurveyManager surveyManager;
	
	protected CollectSurvey loadSurvey() throws IdmlParseException {
		InputStream is = ClassLoader.getSystemResourceAsStream("test.idm.xml");
		CollectSurvey survey = surveyDao.unmarshalIdml(is);
		survey.setName("archenland1");
		return survey;
	}

	protected CollectSurvey importModel() throws SurveyImportException, IdmlParseException {
		CollectSurvey survey = (CollectSurvey) loadSurvey();
		surveyManager.importModel(survey);
		return survey;
	}
	
	protected CollectSurvey createSurvey() {
		CollectSurvey createSurvey = (CollectSurvey) collectSurveyContext.createSurvey();
		return createSurvey;
	}
	
	protected File getSystemResourceFile(String fileName) throws URISyntaxException {
		URL fileUrl = ClassLoader.getSystemResource(fileName);
		File file = new File(fileUrl.toURI());
		return file;
	}
}
