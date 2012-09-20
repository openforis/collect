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

	public static final String SESSION_KEY = "designer_status";
	
	private String currentLanguageCode;
	private CollectSurvey survey;
	
	public String getCurrentLanguageCode() {
		return currentLanguageCode;
	}
	
	public void setCurrentLanguageCode(String currentLanguageCode) {
		this.currentLanguageCode = currentLanguageCode;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
}
