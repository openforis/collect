package org.openforis.idm.model;

import org.openforis.idm.metamodel.BooleanAttributeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class BooleanAttribute extends Attribute<BooleanAttributeDefinition, BooleanValue> {

	private static final long serialVersionUID = 1L;

	public BooleanAttribute(BooleanAttributeDefinition definition) {
		super(definition);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public BooleanValue getValue() {
		Field<Boolean> field = (Field<Boolean>) getField(0);
		return field == null ? null : new BooleanValue(field.getValue());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setValueInFields(BooleanValue value) {
		Field<Boolean> field = (Field<Boolean>) getField(0);
		field.setValue(value == null ? null : value.getValue());
	}

}
