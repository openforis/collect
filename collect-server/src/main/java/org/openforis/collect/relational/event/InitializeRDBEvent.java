package org.openforis.collect.relational.event;

import org.openforis.collect.event.RecordStep;
import org.openforis.collect.event.SurveyEvent;

public class InitializeRDBEvent extends SurveyEvent {
	
	private final RecordStep step;

	public InitializeRDBEvent(String surveyName, RecordStep step) {
		super(surveyName);
		this.step = step;
	}

	public RecordStep getStep() {
		return step;
	}
	
}
