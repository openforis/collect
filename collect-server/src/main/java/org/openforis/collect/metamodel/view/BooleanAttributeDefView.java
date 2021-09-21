package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.idm.metamodel.AttributeType;

public class BooleanAttributeDefView extends AttributeDefView {

	public enum LayoutType {
		CHECKBOX, TEXTBOX
	}

	private LayoutType layoutType;

	public BooleanAttributeDefView(int id, String name, String label, AttributeType type, List<String> fieldNames,
			boolean key, boolean multiple) {
		super(id, name, label, type, fieldNames, key, multiple);
	}

	public LayoutType getLayoutType() {
		return layoutType;
	}

	public void setLayoutType(LayoutType layoutType) {
		this.layoutType = layoutType;
	}

}
