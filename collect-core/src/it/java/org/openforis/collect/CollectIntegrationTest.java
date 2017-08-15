package org.openforis.collect;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class CollectIntegrationTest extends CollectTest {

	@Autowired
	protected CollectSurveyContext collectSurveyContext;
	@Autowired
	protected SurveyManager surveyManager;
	@Autowired
	protected UserGroupManager userGroupManager;
	
	protected CollectSurvey loadSurvey() throws IdmlParseException, SurveyValidationException {
		InputStream is = ClassLoader.getSystemResourceAsStream("test.idm.xml");
		CollectSurvey survey = surveyManager.unmarshalSurvey(is);
		survey.setUserGroup(userGroupManager.getDefaultPublicUserGroup());
		survey.setName("archenland1");
		return survey;
	}

	@SuppressWarnings("deprecation")
	protected CollectSurvey importModel() throws SurveyImportException, IdmlParseException, SurveyValidationException {
		CollectSurvey survey = (CollectSurvey) loadSurvey();
		surveyManager.importModel(survey);
		return survey;
	}
	
	protected CollectSurvey createSurvey() {
		CollectSurvey createSurvey = (CollectSurvey) collectSurveyContext.createSurvey();
		return createSurvey;
	}
	
	protected File getSystemResourceFile(String fileName) {
		try {
			URL fileUrl = ClassLoader.getSystemResource(fileName);
			return new File(fileUrl.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
