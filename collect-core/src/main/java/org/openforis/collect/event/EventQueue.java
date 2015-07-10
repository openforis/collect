package org.openforis.collect.event;

public interface EventQueue {

	void publish(RecordTransaction recordTransaction);
	
	void publish(SurveyEvent surveyEvent);
	
}