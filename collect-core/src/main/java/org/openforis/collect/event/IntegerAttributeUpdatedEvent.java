package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class IntegerAttributeUpdatedEvent extends
		NumberAttributeUpdatedEvent<Integer> {

	public IntegerAttributeUpdatedEvent() {
		super(Integer.class);
	}
}
