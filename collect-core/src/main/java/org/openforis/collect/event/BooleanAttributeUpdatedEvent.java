package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class BooleanAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private Boolean value;

	public Boolean getValue() {
		return value;
	}
	
	public void setValue(Boolean value) {
		this.value = value;
	}

}
