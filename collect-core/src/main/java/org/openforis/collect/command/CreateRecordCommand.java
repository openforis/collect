package org.openforis.collect.command;

public class CreateRecordCommand implements Command {
	
	private static final long serialVersionUID = 1L;
	
	private int surveyId;
	private String username;
	private String formVersion;
	
	public CreateRecordCommand(int surveyId, String username) {
		super();
		this.surveyId = surveyId;
		this.username = username;
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

	public String getFormVersion() {
		return formVersion;
	}
	
	public void setFormVersion(String formVersion) {
		this.formVersion = formVersion;
	}
	
}
