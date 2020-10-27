package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.idm.metamodel.TextAttributeDefinition.Type;

public class TextAttributeDefView extends AttributeDefView {

	private Type textType;

	public TextAttributeDefView(int id, String name, String label, AttributeType type, List<String> fieldNames,
			boolean key, boolean multiple) {
		super(id, name, label, type, fieldNames, key, multiple);
	}

	public Type getTextType() {
		return textType;
	}

	public void setTextType(Type type) {
		this.textType = type;
	}

}
