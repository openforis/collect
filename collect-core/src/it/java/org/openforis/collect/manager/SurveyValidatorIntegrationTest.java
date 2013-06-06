package org.openforis.collect.manager;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
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
	public void skipValidationTest() throws IdmlParseException, IOException, SurveyValidationException {
		URL idm = ClassLoader.getSystemResource("invalid.test.idm.xml");
		InputStream is = idm.openStream();
		CollectSurvey survey = surveyManager.unmarshalSurvey(is);
		assertNotNull(survey);
	}
	
	@Test(expected=SurveyValidationException.class)
	public void invalidSurveyImportTest() throws IdmlParseException, IOException, SurveyValidationException {
		URL idm = ClassLoader.getSystemResource("invalid.test.idm.xml");
		InputStream is = idm.openStream();
		CollectSurvey survey = surveyManager.unmarshalSurvey(is, true);
		assertNull(survey);
	}

	@Test
	public void validSurveyUnmarshallTest() throws IdmlParseException, IOException, SurveyValidationException {
		URL idm = ClassLoader.getSystemResource("test.idm.xml");
		InputStream is = idm.openStream();
		CollectSurvey survey = surveyManager.unmarshalSurvey(is, true);
		assertNotNull(survey);
	}
}
