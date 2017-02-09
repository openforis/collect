package org.openforis.collect.command;

import org.openforis.collect.event.RecordEvent;

public class DeleteRecordCommand implements Command<RecordEvent> {
	
	private static final long serialVersionUID = 1L;
	
	private int surveyId;
	private String username;
	private int recordId;
	
	public DeleteRecordCommand() {
		super();
	}
	
	public DeleteRecordCommand(int surveyId, String username, int recordId) {
		super();
		this.surveyId = surveyId;
		this.username = username;
		this.recordId = recordId;
	}

	public int getSurveyId() {
		return surveyId;
	}
	
	public void setSurveyId(int surveyId) {
		this.surveyId = surveyId;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

}
