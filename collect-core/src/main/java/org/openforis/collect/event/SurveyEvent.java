package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class SurveyEvent {

	private String surveyName;

	public SurveyEvent(String surveyName) {
		super();
		this.surveyName = surveyName;
	}

	public String getSurveyName() {
		return surveyName;
	}
	
}
