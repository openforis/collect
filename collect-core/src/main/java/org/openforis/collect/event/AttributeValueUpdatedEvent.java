package org.openforis.collect.event;

import org.openforis.idm.model.Value;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class AttributeValueUpdatedEvent<V extends Value> extends AttributeEvent {
	
	private V value;

	public V getValue() {
		return value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}
}
