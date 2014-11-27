package org.openforis.collect.datacleansing;

import org.openforis.idm.metamodel.PersistedObject;

/**
 * 
 * @author A. Modragon
 *
 */
public class DataErrorReportItem extends PersistedObject {
	
	public enum Status {
		PENDING('p'),
		FIXED('f');
		
		public static Status fromCode(char code) {
			for (Status status : values()) {
				if (status.code == code) {
					return status;
				}
			}
			return null;
		}
		
		private char code;

		Status(char code) {
			this.code = code;
		}
		
		public char getCode() {
			return code;
		}
		
	}
	
	private DataErrorReport report;
	private int recordId;
	private int parentEntityId;
	private int nodeIndex;
	private String value;
	private Status status;

	public DataErrorReportItem(DataErrorReport report) {
		super();
		this.report = report;
	}

	public DataErrorReport getReport() {
		return report;
	}

	public void setReport(DataErrorReport report) {
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
