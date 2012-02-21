/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyDAO;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.Survey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
public class SurveyManager {
	// private EntityManager entityManager;
	private Map<String, Survey> surveysByName;
	private Map<Integer, Survey> surveysById;
	private List<Survey> surveys;

	public SurveyManager() {
		surveysById = new HashMap<Integer, Survey>();
		surveysByName = new HashMap<String, Survey>();
	}

	@Autowired
	private SurveyDAO surveyDAO;

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
		}
	}

}
