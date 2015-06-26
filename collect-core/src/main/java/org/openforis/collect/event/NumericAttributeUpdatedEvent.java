package org.openforis.collect.event;

import java.util.Date;

public abstract class NumericAttributeUpdatedEvent<T extends Number> extends AttributeUpdatedEvent {
	
	private final Class<T> valueType;
	private final Integer unitId;

	public NumericAttributeUpdatedEvent(Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Class<T> valueType, Integer unitId, Date timestamp, String userName) {
		super(recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
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
