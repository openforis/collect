package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.metamodel.ui.UIField;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;

public class UIFieldView extends UIModelObjectView<UIField> implements UITabComponentView<UIField> {

	public UIFieldView(UIField uiObject) {
		super(uiObject);
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
	
	public String getLabel() {
		AttributeDefinition attrDef = (AttributeDefinition) getNodeDefinition(uiObject.getAttributeDefinitionId());
		return attrDef.getLabel(Type.INSTANCE);
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