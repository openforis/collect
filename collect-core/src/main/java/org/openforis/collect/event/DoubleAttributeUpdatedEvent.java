package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class DoubleAttributeUpdatedEvent extends
		NumberAttributeUpdatedEvent<Double> {

	public DoubleAttributeUpdatedEvent() {
		super(Double.class);
	}

}
