package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class NumberAttributeUpdatedEvent<T extends Number> extends
		NumericAttributeUpdatedEvent<T> {

	private T value;
	
	public NumberAttributeUpdatedEvent(Class<T> valueType) {
		super(valueType);
	}

	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}

}
