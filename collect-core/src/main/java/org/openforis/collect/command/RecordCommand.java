package org.openforis.collect.command;

import org.openforis.collect.event.RecordStep;

public abstract class RecordCommand implements Command {

	private static final long serialVersionUID = 1L;

	private String username;
	private int surveyId;
	private Integer recordId;
	private RecordStep recordStep;
	private String preferredLanguage;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getSurveyId() {
		return surveyId;
	}

	public void setSurveyId(int surveyId) {
		this.surveyId = surveyId;
	}

	public Integer getRecordId() {
		return recordId;
	}

	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}

	public RecordStep getRecordStep() {
		return recordStep;
	}

	public void setRecordStep(RecordStep recordStep) {
		this.recordStep = recordStep;
	}

	public String getPreferredLanguage() {
		return preferredLanguage;
	}

	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}

}