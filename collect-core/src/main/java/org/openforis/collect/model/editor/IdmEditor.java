package org.openforis.collect.model.editor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Wibowo, Eko
 *
 */
public class IdmEditor {
	@Autowired
	protected SurveyDao surveyDao;

	@Autowired
	protected RecordDao recordDao;
	@Autowired
	protected ExpressionFactory expressionFactory;
	@Autowired
	protected Validator validator;
	public Survey updateModel() throws IOException, InvalidIdmlException,
			SurveyImportException {

		URL idm = ClassLoader
				.getSystemResource("MOFOR_WORKING_update.idnfi.idm.xml");// MOFOR_2012_04_03_update.idnfi.idm.xml
		InputStream is = idm.openStream();
		CollectIdmlBindingContext idmlBindingContext = surveyDao
				.getBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext
				.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("idnfi");
		surveyDao.updateModel(survey);
		return survey;
	}

	public Survey importIdnfi() throws IOException, SurveyImportException,
			InvalidIdmlException {
		URL idm = ClassLoader.getSystemResource("idnfi.idm.xml");
		InputStream is = idm.openStream();
		CollectIdmlBindingContext idmlBindingContext = surveyDao
				.getBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext
				.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("idnfi");
		surveyDao.clearModel();
		surveyDao.importModel(survey);
		return survey;
	}
}
