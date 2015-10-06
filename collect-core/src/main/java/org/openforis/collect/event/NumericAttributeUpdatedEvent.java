package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class NumericAttributeUpdatedEvent<T extends Number> extends
		AttributeUpdatedEvent {

	private final Class<T> valueType;
	private final Integer unitId;

	public NumericAttributeUpdatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, Class<T> valueType, Integer unitId, Date timestamp,
			String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				timestamp, userName);
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
