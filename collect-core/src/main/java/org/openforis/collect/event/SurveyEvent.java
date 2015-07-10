package org.openforis.collect.event;

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
