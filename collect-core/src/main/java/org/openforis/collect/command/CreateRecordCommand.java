package org.openforis.collect.command;

import java.util.ArrayList;
import java.util.List;

public class CreateRecordCommand implements Command {
	
	private static final long serialVersionUID = 1L;
	
	private int surveyId;
	private String username;
	private String formVersion;
	private List<String> keyValues = new ArrayList<String>();
	
	public CreateRecordCommand() {
		super();
	}
	
	public CreateRecordCommand(int surveyId, String username) {
		this();
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
	
	public List<String> getKeyValues() {
		return keyValues;
	}

	public void setKeyValues(List<String> keyValues) {
		this.keyValues = keyValues;
	}
	
}
