package org.openforis.collect.event;

import java.util.Date;

public abstract class RangeAttributeUpdatedEvent<T extends Number> extends NumericAttributeUpdatedEvent<T> {

	private final T from;
	private final T to;

	public RangeAttributeUpdatedEvent(Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, T from, T to, Class<T> valueType,
			Integer unitId, Date timestamp, String userName) {
		super(recordId, definitionId, parentEntityId, nodeId, valueType, unitId,
				timestamp, userName);
		this.from = from;
		this.to = to;
	}
	
	public T getFrom() {
		return from;
	}
	
	public T getTo() {
		return to;
	}
	
}
