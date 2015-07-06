package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class RecordEvent {

	private String surveyName;
	private Integer recordId;
	private final String definitionId;
	private final String nodeId;
	private final Date timestamp;
	private final String userName;
	private List<String> ancestorIds;
	
	public RecordEvent(String surveyName, Integer recordId, String definitionId, 
			List<String> ancestorIds, String nodeId, Date timestamp, String userName) {
		super();
		this.surveyName = surveyName;
		this.recordId = recordId;
		this.definitionId = definitionId;
		this.ancestorIds = ancestorIds;
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
	
	public String getDefinitionId() {
		return definitionId;
	}
	
	public String getNodeId() {
		return nodeId;
	}
	
	public List<String> getAncestorIds() {
		return ancestorIds;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public String getUserName() {
		return userName;
	}

}
