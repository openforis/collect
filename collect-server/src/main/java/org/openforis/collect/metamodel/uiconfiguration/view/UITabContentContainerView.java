package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.List;

import org.openforis.collect.metamodel.ui.UIFormContentContainer;

public abstract class UITabContentContainerView<O extends UIFormContentContainer> extends UIModelObjectView<O> {

	public UITabContentContainerView(O uiObject) {
		super(uiObject);
	}
	
	public List<UITabView> getTabs() {
		return Views.fromObjects(uiObject.getForms(), UITabView.class);
	}
	
	public List<UITabComponentView<?>> getChildren() {
		return UITabComponentViews.fromObjects(uiObject.getChildren());
	}
	
	public int getTotalColumns() {
		return uiObject.getTotalColumns();
	}
	
	public int getTotalRows() {
		return uiObject.getTotalRows();
	}
}