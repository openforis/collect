/**
 * 
 */
package org.openforis.collect.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class SurveyManager {

	@Autowired
	private ExpressionFactory expressionFactory;
	
	@Autowired
	private Validator validator;
	
	@Autowired
	private SurveyDao surveyDao;
	
	private Map<String, CollectSurvey> surveysByName;
	private Map<Integer, CollectSurvey> surveysById;
	private List<CollectSurvey> surveys;

	public SurveyManager() {
		surveysById = new HashMap<Integer, CollectSurvey>();
		surveysByName = new HashMap<String, CollectSurvey>();
	}

	public List<CollectSurvey> getAll() {
		return CollectionUtil.unmodifiableList(surveys);
	}

	@Transactional
	public CollectSurvey get(String name) {
		CollectSurvey survey = surveysByName.get(name);
		return survey;
	}

	@Transactional
	public void importModel(CollectSurvey survey) throws SurveyImportException {
		surveyDao.importModel(survey);
		initSurvey(survey);
	}

	@Transactional
	public List<SurveySummary> getSurveySummaries(String lang) {
		List<SurveySummary> summaries = new ArrayList<SurveySummary>();
		for (Survey survey : surveys) {
			Integer id = survey.getId();
			String projectName = getProjectName(survey, lang);
			String name = survey.getName();
			SurveySummary summary = new SurveySummary(id, name, projectName);
			summaries.add(summary);
		}
		return summaries;
	}
	
	public String marshalSurvey(Survey survey)  {
		try {
			String result = surveyDao.marshalSurvey(survey);
			return result;
		} catch (SurveyImportException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void marshalSurvey(Survey survey, OutputStream os)  {
		try {
			surveyDao.marshalSurvey(survey, os);
		} catch (SurveyImportException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public CollectSurvey unmarshalSurvey(InputStream is) throws InvalidIdmlException {
		CollectSurveyContext surveyContext = new CollectSurveyContext(expressionFactory, validator, null);
		CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext(surveyContext);
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		try {
			byte[] bytes = IOUtils.toByteArray(is);
			surveyUnmarshaller.validateAgainstSchema(bytes);
			CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(bytes);
			return survey;
		} catch (IOException e) {
			throw new InvalidIdmlException("Error reading input stream");
		}
	}
	
	private String getProjectName(Survey survey, String lang) {
		List<LanguageSpecificText> names = survey.getProjectNames();
		if (names == null || names.size() == 0) {
			return "";
		} else if (names.size() == 1) {
			return names.get(0).getText();
		} else {
			for (LanguageSpecificText text : names) {
				if (lang.equalsIgnoreCase(text.getLanguage())) {
					return text.getText();
				}
			}
		}
		return "";
	}

	@Transactional
	protected void init() {
		surveys = surveyDao.loadAll();
		for (CollectSurvey survey : surveys) {
			initSurvey(survey);
		}
	}

	private void initSurvey(CollectSurvey survey) {
		surveysById.put(survey.getId(), survey);
		surveysByName.put(survey.getName(), survey);
	}

}
