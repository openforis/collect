/**
 * 
 */
package org.openforis.collect.designer.session;

import org.openforis.collect.model.CollectSurvey;

/**
 * @author S. Ricci
 *
 */
public class SessionStatus {

	private String selectedLanguageCode;
	private CollectSurvey survey;
	
	public String getSelectedLanguageCode() {
		return selectedLanguageCode;
	}
	public void setSelectedLanguageCode(String selectedLanguageCode) {
		this.selectedLanguageCode = selectedLanguageCode;
	}
	public CollectSurvey getSurvey() {
		return survey;
	}
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	
	
}
