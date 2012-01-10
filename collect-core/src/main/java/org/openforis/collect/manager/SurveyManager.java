/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyDAO;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.Survey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class SurveyManager {
	// private EntityManager entityManager;

	private List<Survey> surveys;

	@Autowired
	private SurveyDAO surveyDAO;

	public Survey load(String name) {
		Survey survey = surveyDAO.load(name);
		return survey;
	}

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

	@SuppressWarnings("unused")
	private void init() {
		surveys = surveyDAO.loadAll();
	}

}
