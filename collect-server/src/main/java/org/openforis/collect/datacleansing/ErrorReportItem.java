package org.openforis.collect.datacleansing;

import org.openforis.idm.metamodel.PersistedObject;

/**
 * 
 * @author A. Modragon
 *
 */
public class ErrorReportItem extends PersistedObject {
	
	private ErrorReport report;
	private int recordId;
	private int parentEntityId;
	private int nodeIndex;
	private String value;
	private char status;

	public ErrorReport getReport() {
		return report;
	}

	public void setReport(ErrorReport report) {
		this.report = report;
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

	public int getNodeIndex() {
		return nodeIndex;
	}

	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public char getStatus() {
		return status;
	}

	public void setStatus(char status) {
		this.status = status;
	}
	
}
