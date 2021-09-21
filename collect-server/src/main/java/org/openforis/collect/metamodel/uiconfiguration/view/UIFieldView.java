package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.metamodel.ui.UIField;
import org.openforis.collect.metamodel.view.ViewContext;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AttributeType;

public class UIFieldView<O extends UIField> extends UIModelObjectView<O> implements UITabComponentView<O> {

	public UIFieldView(O uiObject, ViewContext context) {
		super(uiObject, context);
	}
	
	@Override
	public String getType() {
		boolean multiple = uiObject.getAttributeDefinition().isMultiple();
		return multiple ? "MULTIPLE_FIELD" : "FIELD";
	}
	
	public String getAttributeType() {
		AttributeDefinition attrDef = uiObject.getAttributeDefinition();
		AttributeType attrType = AttributeType.valueOf(attrDef);
		return attrType.name();
	}
	
	public Integer getAttributeDefinitionId() {
		return uiObject.getAttributeDefinitionId();
	}
	
	@Override
	public int getColumn() {
		return uiObject.getColumn();
	}
	
	@Override
	public int getColumnSpan() {
		return uiObject.getColumnSpan();
	}
	
	@Override
	public int getRow() {
		return uiObject.getRow();
	}

}