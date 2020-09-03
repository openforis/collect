package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.metamodel.ui.UIForm;
import org.openforis.collect.metamodel.view.ViewContext;

public class UITabView extends UITabContentContainerView<UIForm> {

	public UITabView(UIForm uiObject, ViewContext context) {
		super(uiObject, context);
	}
	
	@Override
	public String getType() {
		return "TAB";
	}
	
	public String getLabel() {
		return getLabel(context.getLanguage());
	}
	
	public String getLabel(String language) {
		return uiObject.getLabel(language, getSurvey().getDefaultLanguage());
	}
}