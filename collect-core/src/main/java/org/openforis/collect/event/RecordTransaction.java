package org.openforis.collect.event;

import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class RecordTransaction extends SurveyEvent {

	private int recordId;
	private RecordStep recordStep;
	private List<? extends RecordEvent> events;
	
	public RecordTransaction(String surveyName, int recordId,
			RecordStep recordStep, List<? extends RecordEvent> events) {
		super(surveyName);
		this.recordId = recordId;
		this.recordStep = recordStep;
		this.events = events;
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
