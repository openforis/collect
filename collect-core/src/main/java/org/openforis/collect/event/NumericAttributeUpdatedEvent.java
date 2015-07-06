package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public abstract class NumericAttributeUpdatedEvent<T extends Number> extends AttributeUpdatedEvent {
	
	private final Class<T> valueType;
	private final Integer unitId;

	public NumericAttributeUpdatedEvent(String surveyName, Integer recordId, String definitionId, 
			List<String> ancestorIds, String nodeId,
			Class<T> valueType, Integer unitId, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, ancestorIds, nodeId, timestamp, userName);
		this.valueType = valueType;
		this.unitId = unitId;
	}

	public Class<T> getValueType() {
		return valueType;
	}
	
	public Integer getUnitId() {
		return unitId;
	}

}
