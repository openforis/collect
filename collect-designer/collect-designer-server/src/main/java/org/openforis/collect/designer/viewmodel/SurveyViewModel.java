/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyViewModel {
	
	private static final String ENGLISH_LANGUAGE_CODE = "eng";

	private CollectSurvey survey;
	
	private String selectedLanguageCode;
	
	public SurveyViewModel() {
		selectedLanguageCode = ENGLISH_LANGUAGE_CODE;
		survey = new CollectSurvey();
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public String getProjectName() {
		return survey.getProjectName(selectedLanguageCode);
	}
	
	public void setProjectName(String name) {
		survey.setProjectName(selectedLanguageCode, name);
	}
	
	public String getDescription() {
		return survey.getDescription(selectedLanguageCode);
	}
	
	public void setDescription(String description) {
		survey.setDescription(selectedLanguageCode, description);
	}

	public String getSelectedLanguageCode() {
		return selectedLanguageCode;
	}

	public void setSelectedLanguageCode(String selectedLanguageCode) {
		this.selectedLanguageCode = selectedLanguageCode;
	}
	
}
