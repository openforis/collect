package org.openforis.collect.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyValidatorIntegrationTest extends CollectIntegrationTest {
	
	@Autowired
	private SurveyManager surveyManager;
	
	@Test
	public void skipValidationTest() throws SurveyValidationException, SurveyImportException {
		InputStream is = ClassLoader.getSystemResourceAsStream("invalid.test.idm.xml");
		CollectSurvey survey = surveyManager.importModel(is, "test_survey", false);
		assertNotNull(survey);
	}
	
	@Test(expected=SurveyValidationException.class)
	public void invalidSurveyImportTest() throws SurveyValidationException, SurveyImportException {
		InputStream is = ClassLoader.getSystemResourceAsStream("invalid.test.idm.xml");
		CollectSurvey survey = surveyManager.importModel(is, "test_survey", true);
		assertNull(survey);
	}

	@Test
	public void validSurveyUnmarshallTest() throws SurveyValidationException, SurveyImportException {
		InputStream is = ClassLoader.getSystemResourceAsStream("test.idm.xml");
		CollectSurvey survey = surveyManager.importModel(is, "test_survey", false);
		assertNotNull(survey);
	}
}
