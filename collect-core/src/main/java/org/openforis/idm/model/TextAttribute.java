package org.openforis.idm.model;

import org.openforis.idm.metamodel.TextAttributeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class TextAttribute extends Attribute<TextAttributeDefinition, TextValue> {

	private static final long serialVersionUID = 1L;

	public TextAttribute(TextAttributeDefinition definition) {
		super(definition);
	}
	
	@SuppressWarnings("unchecked")
	public Field<String> getTextField() {
		return (Field<String>) getField(0);
	}

	public String getText() {
		return getTextField().getValue();
	}

	public void setText(String value) {
		getTextField().setValue(value);
	}
	
	@Override
	public TextValue getValue() {
		return new TextValue(getText());
	}

	@Override
	protected void setValueInFields(TextValue value) {
		getTextField().setValue(value.getValue());
	}
	
}
