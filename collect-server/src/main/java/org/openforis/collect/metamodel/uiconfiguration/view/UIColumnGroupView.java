package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.List;

import org.openforis.collect.metamodel.ui.UIColumnGroup;
import org.openforis.collect.metamodel.view.ViewContext;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;

public class UIColumnGroupView extends UITableHeadingComponentView<UIColumnGroup> {

	public UIColumnGroupView(UIColumnGroup uiObject, ViewContext context) {
		super(uiObject, context);
	}
	
	@Override
	public String getType() {
		return "COLUMN_GROUP";
	}

	public int getEntityDefinitionId() {
		return uiObject.getEntityDefinitionId();
	}
	
	public List<UITableHeadingComponentView<?>> getHeadingComponents() {
		return UITableHeadingComponentView.fromObjects(uiObject.getHeadingComponents(), context);
	}
	
	public String getLabel() {
		NodeDefinition nodeDef = getNodeDefinition(getEntityDefinitionId());
		String label = nodeDef.getFailSafeLabel(Type.ABBREVIATED, Type.INSTANCE);
		return label;
	}
}