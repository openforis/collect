package org.openforis.collect.datacleansing;

import java.util.List;

import org.json.simple.JSONObject;
import org.openforis.idm.metamodel.PersistedObject;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Value;

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
	
	@SuppressWarnings("unchecked")
	public void setAttributeValue(Attribute<?, ?> attr) {
		if (attr.isEmpty()) {
			value = null;
		}
		JSONObject jsonObj = new JSONObject();
		List<Field<?>> fields = attr.getFields();
		for (Field<?> field : fields) {
			jsonObj.put(field.getName(), field.getValue());
		}
		value = jsonObj.toJSONString();
	}
	
	public Value getAttributeValue() {
		
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
