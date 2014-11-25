package org.openforis.collect.datacleansing;

/**
 * 
 * @author A. Modragon
 *
 */
public class ErrorReportItem {
	
	private Integer id;
	private ErrorReport report;
	private int parentEntityId;
	private int nodeIndex;
	private String value;
	private char status;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ErrorReport getReport() {
		return report;
	}

	public void setReport(ErrorReport report) {
		this.report = report;
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
