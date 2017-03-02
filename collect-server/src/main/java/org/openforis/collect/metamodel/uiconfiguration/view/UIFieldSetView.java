package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.metamodel.ui.UIFormSection;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;

public class UIFieldSetView extends UITabContentContainerView<UIFormSection> implements UITabComponentView<UIFormSection> {

	public UIFieldSetView(UIFormSection uiObject) {
		super(uiObject);
	}
	
	@Override
	public String getType() {
		boolean multiple = uiObject.getEntityDefinition().isMultiple();
		return (multiple ? "MULTIPLE_" : "") + "FIELDSET";
	}
	
	public Integer getEntityDefinitionId() {
		return uiObject.getEntityDefinitionId();
	}
	
	public String getLabel() {
		NodeDefinition entityDef = getNodeDefinition(getEntityDefinitionId());
		return entityDef.getLabel(Type.INSTANCE);
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