package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class NumericAttributeUpdatedEvent<T extends Number> extends
		AttributeUpdatedEvent {

	private final Class<T> valueType;
	private Integer unitId;

	public NumericAttributeUpdatedEvent(Class<T> valueType) {
		super();
		this.valueType = valueType;
	}

	public Class<T> getValueType() {
		return valueType;
	}

	public Integer getUnitId() {
		return unitId;
	}
	
	public void setUnitId(Integer unitId) {
		this.unitId = unitId;
	}

}
