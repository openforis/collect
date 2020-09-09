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
	private RecordStep recordStep;
	private String definitionId;
	private List<String> ancestorIds;
	private String nodeId;
	private String nodePath;
	private String parentEntityPath;
	private Date timestamp;
	private String userName;
	
	public RecordEvent() {
	}
	
	public RecordEvent(String surveyName, Integer recordId, RecordStep recordStep, String definitionId, 
			List<String> ancestorIds, String nodeId, Date timestamp, String userName) {
		super();
		this.surveyName = surveyName;
		this.recordId = recordId;
		this.recordStep = recordStep;
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
	
	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
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
	
	public String getDefinitionId() {
		return definitionId;
	}
	
	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}
	
	public String getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	public String getNodePath() {
		return nodePath;
	}
	
	public void setNodePath(String nodePath) {
		this.nodePath = nodePath;
	}
	
	public String getParentEntityPath() {
		return parentEntityPath;
	}
	
	public void setParentEntityPath(String parentEntityPath) {
		this.parentEntityPath = parentEntityPath;
	}
	
	public List<String> getAncestorIds() {
		return ancestorIds;
	}
	
	public void setAncestorIds(List<String> ancestorIds) {
		this.ancestorIds = ancestorIds;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}

}
