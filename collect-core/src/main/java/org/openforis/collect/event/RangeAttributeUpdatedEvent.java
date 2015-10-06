package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class RangeAttributeUpdatedEvent<T extends Number> extends
		NumericAttributeUpdatedEvent<T> {

	private final T from;
	private final T to;

	public RangeAttributeUpdatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, T from, T to, Class<T> valueType, Integer unitId,
			Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				valueType, unitId, timestamp, userName);
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
