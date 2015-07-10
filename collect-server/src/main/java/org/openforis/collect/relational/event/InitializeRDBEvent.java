package org.openforis.collect.relational.event;

import org.openforis.collect.event.RecordStep;

public class InitializeRDBEvent {
	
	private final String surveyName;
	private final RecordStep step;

	public InitializeRDBEvent(String surveyName, RecordStep step) {
		super();
		this.surveyName = surveyName;
		this.step = step;
	}

	public String getSurveyName() {
		return surveyName;
	}
	
	public RecordStep getStep() {
		return step;
	}
	
}
