package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class RangeAttributeUpdatedEvent<T extends Number> extends
		NumericAttributeUpdatedEvent<T> {

	private T from;
	private T to;

	public RangeAttributeUpdatedEvent(Class<T> valueType) {
		super(valueType);
	}

	public T getFrom() {
		return from;
	}
	
	public void setFrom(T from) {
		this.from = from;
	}

	public T getTo() {
		return to;
	}
	
	public void setTo(T to) {
		this.to = to;
	}

}
