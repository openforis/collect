package org.openforis.collect.event;

import java.util.List;

public class RecordTransaction {

	private String surveyName;
	private int recordId;
	private RecordStep recordStep;
	private List<? extends RecordEvent> events;
	
	public RecordTransaction(String surveyName, int recordId,
			RecordStep recordStep, List<? extends RecordEvent> events) {
		super();
		this.surveyName = surveyName;
		this.recordId = recordId;
		this.recordStep = recordStep;
		this.events = events;
	}

	public String getSurveyName() {
		return surveyName;
	}

	public int getRecordId() {
		return recordId;
	}

	public RecordStep getRecordStep() {
		return recordStep;
	}

	public List<? extends RecordEvent> getEvents() {
		return events;
	}

}
