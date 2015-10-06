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
	private final String definitionId;
	private List<String> ancestorIds;
	private final String nodeId;
	private final Date timestamp;
	private final String userName;
	
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
	
	public Integer getRecordId() {
		return recordId;
	}
	
	public RecordStep getRecordStep() {
		return recordStep;
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
