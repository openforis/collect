package org.openforis.collect.event;

import org.openforis.idm.model.Value;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class AttributeCreatedEvent extends AttributeEvent {

	private Value value;
	
	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}
	
}
