/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.SurveyDependencies;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyDAO;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
public class SurveyManager {

	@Autowired
	private SurveyDAO surveyDAO;
	@Autowired
	private ExpressionFactory expressionFactory;

	private Map<String, Survey> surveysByName;
	private Map<Integer, Survey> surveysById;
	private List<Survey> surveys;
	private Map<String, SurveyDependencies> surveyDependenciesMap;

	public SurveyManager() {
		surveysById = new HashMap<Integer, Survey>();
		surveysByName = new HashMap<String, Survey>();
		surveyDependenciesMap = new HashMap<String, SurveyDependencies>();
	}

	public List<Survey> getAll() {
		return CollectionUtil.unmodifiableList(surveys);
	}

	@Transactional
	public Survey get(String name) {
		Survey survey = surveysByName.get(name);
		return survey;
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
		surveys = surveyDAO.loadAll();
		for (Survey survey : surveys) {
			surveysById.put(survey.getId(), survey);
			surveysByName.put(survey.getName(), survey);

			SurveyDependencies surveyDependencies = new SurveyDependencies(expressionFactory);
			surveyDependencies.register(survey);
			surveyDependenciesMap.put(survey.getName(), surveyDependencies);
		}
	}

}
