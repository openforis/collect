package org.openforis.collect.dataview;

import java.util.HashMap;
import java.util.Map;

public class QueryResultRow {
	
	private Map<Integer, String> valueByAttributeDefinitionId = new HashMap<Integer, String>();

	public void putValueByDefinitionId(int attributeDefinitionId, String value) {
		valueByAttributeDefinitionId.put(attributeDefinitionId, value);
	}

	public Map<Integer, String> getValueByAttributeDefinitionId() {
		return valueByAttributeDefinitionId;
	}

	public void setValueByAttributeDefinitionId(Map<Integer, String> valueByAttributeDefinitionId) {
		this.valueByAttributeDefinitionId = valueByAttributeDefinitionId;
	}
}