package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.idm.metamodel.AttributeType;
import org.openforis.idm.metamodel.TextAttributeDefinition.Type;

public class TextAttributeDefView extends AttributeDefView {

	public enum TextTransform {
		NONE, UPPERCASE, LOWERCASE, CAMELCASE;
	}

	private Type textType;
	private TextTransform textTransform;
	private boolean geometry;

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

	public TextTransform getTextTransform() {
		return textTransform;
	}

	public void setTextTransform(TextTransform textTransform) {
		this.textTransform = textTransform;
	}
	
	public boolean isGeometry() {
		return geometry;
	}
	
	public void setGeometry(boolean geometry) {
		this.geometry = geometry;
	}
}
