package org.openforis.collect.datacleansing;

import java.util.List;

import org.openforis.collect.datacleansing.json.JSONValueParser;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AbstractPersistedObject;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;

/**
 * 
 * @author A. Modragon
 *
 */
public class DataErrorReportItem extends AbstractPersistedObject {
	
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
	private CollectRecord record;
	private int recordId;
	private int parentEntityId;
	private int nodeIndex;
	private String value;
	private Status status;

	public DataErrorReportItem(DataErrorReport report) {
		super();
		this.report = report;
	}
	
	public AttributeDefinition getAttributeDefinition() {
		DataQuery query = report.getQuery();
		Schema schema = query.getSchema();
		AttributeDefinition def = (AttributeDefinition) schema.getDefinitionById(query.getAttributeDefinitionId());
		return def;
	}
	
	public Value extractAttributeValue() {
		AttributeDefinition def = getAttributeDefinition();
		Value val = new JSONValueParser().parseValue(def, value);
		return val;
	}

	public List<String> getRecordKeyValues() {
		return record == null ? null: record.getRootEntityKeyValues();
	}
	
	public String extractNodePath() {
		Survey survey = record.getSurvey();
		DataErrorQuery query = report.getQuery();
		NodeDefinition attrDefn = survey.getSchema().getDefinitionById(query.getAttributeDefinitionId());
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
		String path = String.format("%s/%s[%d]", parentEntity.getPath(), attrDefn.getName(), nodeIndex + 1);
		return path;
	}
	
	public CollectRecord getRecord() {
		return record;
	}
	
	public void setRecord(CollectRecord record) {
		this.record = record;
	}
	
	public DataErrorReport getReport() {
		return report;
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
