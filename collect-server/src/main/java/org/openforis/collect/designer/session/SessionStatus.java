/**
 * 
 */
package org.openforis.collect.designer.session;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ModelVersion;

/**
 * @author S. Ricci
 *
 */
public class SessionStatus {

	public static final String SESSION_KEY = "designer_status";
	
	private String currentLanguageCode;
	private CollectSurvey survey;
	private ModelVersion layoutFormVersion;
	
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

	public ModelVersion getLayoutFormVersion() {
		return layoutFormVersion;
	}

	public void setLayoutFormVersion(ModelVersion layoutFormVersion) {
		this.layoutFormVersion = layoutFormVersion;
	}
	
}
