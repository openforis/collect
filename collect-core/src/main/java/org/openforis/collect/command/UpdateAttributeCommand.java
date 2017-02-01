package org.openforis.collect.command;

public abstract class UpdateAttributeCommand implements Command {
	
	private static final long serialVersionUID = 1L;
	
	private String username;
	private int surveyId;
	private int recordId;
	private int parentEntityId;
	private int attributeDefId;
	
	public UpdateAttributeCommand() {
	}
	
	@Override
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
	
	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public int getParentEntityId() {
		return parentEntityId;
	}

	public void setParentEntityId(int parentEntityId) {
		this.parentEntityId = parentEntityId;
	}

	public int getAttributeDefId() {
		return attributeDefId;
	}

	public void setAttributeDefId(int attributeDefId) {
		this.attributeDefId = attributeDefId;
	}

}
