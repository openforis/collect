package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class NumberAttributeUpdatedEvent<T extends Number> extends
		NumericAttributeUpdatedEvent<T> {

	private final T value;

	public NumberAttributeUpdatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, T value, Class<T> valueType, Integer unitId,
			Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				valueType, unitId, timestamp, userName);
		this.value = value;
	}

	public T getValue() {
		return value;
	}

}
