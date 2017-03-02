package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.metamodel.ui.UIForm;

public class UITabView extends UITabContentContainerView<UIForm> {

	public UITabView(UIForm uiObject) {
		super(uiObject);
	}
	
	@Override
	public String getType() {
		return "TAB";
	}
	
	public String getLabel() {
		//TODO do not use default language
		return uiObject.getLabel(getSurvey().getDefaultLanguage());
	}
}