package org.openforis.collect.event;

import java.util.Date;

public abstract class NumberAttributeUpdatedEvent<T extends Number> extends NumericAttributeUpdatedEvent<T> {

	private final T value;
	
	public NumberAttributeUpdatedEvent(Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, T value, Class<T> valueType,
			Integer unitId, Date timestamp, String userName) {
		super(recordId, definitionId, parentEntityId, nodeId, valueType, unitId,
				timestamp, userName);
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
}
