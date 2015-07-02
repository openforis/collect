package org.openforis.collect.event;

import java.util.Date;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class RecordEvent {

	private String surveyName;
	private Integer recordId;
	private final int definitionId;
	private final Integer parentEntityId;
	private final int nodeId;
	private final Date timestamp;
	private final String userName;
	
	public RecordEvent(String surveyName, Integer recordId, int definitionId, Integer parentEntityId, int nodeId, Date timestamp, String userName) {
		super();
		this.surveyName = surveyName;
		this.recordId = recordId;
		this.definitionId = definitionId;
		this.parentEntityId = parentEntityId;
		this.nodeId = nodeId;
		this.timestamp = timestamp;
		this.userName = userName;
	}
	
	public void initializeRecordId(int recordId) {
		this.recordId = recordId;
	}
	
	public String getSurveyName() {
		return surveyName;
	}
	
	public Integer getRecordId() {
		return recordId;
	}
	
	public int getDefinitionId() {
		return definitionId;
	}
	
	public Integer getParentEntityId() {
		return parentEntityId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public String getUserName() {
		return userName;
	}

}
