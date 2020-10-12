package org.openforis.collect.command;

import org.openforis.idm.model.Value;

public abstract class UpdateAttributeCommand<V extends Value> extends NodeCommand {

	private static final long serialVersionUID = 1L;
	
	private V value;
	
	public V getValue() {
		return value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}
	
}
