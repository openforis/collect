package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.metamodel.ui.UIFormSet;

public class UITabSetView extends UITabContentContainerView<UIFormSet> {
	
	public UITabSetView(UIFormSet uiFormSet) {
		super(uiFormSet);
	}

	@Override
	public String getType() {
		return "TABSET";
	}

	public int getRootEntityDefinitionId() {
		return uiObject.getRootEntityDefinitionId();
	}
}