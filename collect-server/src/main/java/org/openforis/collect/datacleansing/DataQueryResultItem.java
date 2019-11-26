package org.openforis.collect.datacleansing;

import java.util.List;
import java.util.UUID;

import org.openforis.collect.datacleansing.json.JSONValueParser;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.AbstractPersistedObject;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataQueryResultItem extends AbstractPersistedObject<Integer> {
	
	private UUID uuid;
	private DataQuery query;
	private CollectRecord record;
	private int recordId;
	private int parentEntityId;
	private int nodeIndex;
	private String value;
	private Node<?> node;

	public DataQueryResultItem(DataQuery query) {
		this(query, UUID.randomUUID());
	}
	
	public DataQueryResultItem(DataQuery query, UUID uuid) {
		super();
		this.query = query;
		this.uuid = uuid;
	}
	
	public AttributeDefinition getAttributeDefinition() {
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
		NodeDefinition attrDefn = survey.getSchema().getDefinitionById(query.getAttributeDefinitionId());
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
		String path;
		if (attrDefn.isMultiple()) {
			path = String.format("%s/%s[%d]", parentEntity.getPath(), attrDefn.getName(), nodeIndex + 1);
		} else {
			path = String.format("%s/%s", parentEntity.getPath(), attrDefn.getName());
		}
		return path;
	}
	
	public DataQuery getQuery() {
		return query;
	}
	
	public CollectRecord getRecord() {
		return record;
	}
	
	public void setRecord(CollectRecord record) {
		this.record = record;
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
	
	public Node<?> getNode() {
		return node;
	}

	public void setNode(Node<?> node) {
		this.node = node;
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
	
	public UUID getUuid() {
		return uuid;
	}
	
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

}
