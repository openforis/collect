package org.openforis.collect.command;

import java.util.List;

import org.openforis.idm.model.Value;

public abstract class UpdateMultipleAttributeCommand<V extends Value> extends UpdateAttributeCommand<V> {

	private static final long serialVersionUID = 1L;

	private List<V> values;

	public List<V> getValues() {
		return values;
	}

	public void setValues(List<V> values) {
		this.values = values;
	}
}
