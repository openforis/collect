/**
 * 
 */
package org.openforis.collect.designer.session;

import java.io.Serializable;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ModelVersion;

/**
 * @author S. Ricci
 *
 */
public class SessionStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String SESSION_KEY = "designer_status";
	
	private String currentLanguageCode;
	private CollectSurvey survey;
	private Integer publishedSurveyId;
	private ModelVersion layoutFormVersion;
	
	public void reset() {
		currentLanguageCode = null;
		survey = null;
		layoutFormVersion = null;
	}

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

	public Integer getPublishedSurveyId() {
		return publishedSurveyId;
	}

	public void setPublishedSurveyId(Integer publishedSurveyId) {
		this.publishedSurveyId = publishedSurveyId;
	}

}
