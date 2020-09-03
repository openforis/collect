package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.metamodel.ui.UIFormSet;
import org.openforis.collect.metamodel.view.ViewContext;

public class UITabSetView extends UITabContentContainerView<UIFormSet> {
	
	public UITabSetView(UIFormSet uiFormSet, ViewContext context) {
		super(uiFormSet, context);
	}

	@Override
	public String getType() {
		return "TABSET";
	}

	public int getRootEntityDefinitionId() {
		return uiObject.getRootEntityDefinitionId();
	}
}