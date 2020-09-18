package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.List;

import org.openforis.collect.metamodel.ui.UIFormContentContainer;
import org.openforis.collect.metamodel.view.ViewContext;

public abstract class UITabContentContainerView<O extends UIFormContentContainer> extends UIModelObjectView<O> {

	public UITabContentContainerView(O uiObject, ViewContext context) {
		super(uiObject, context);
	}
	
	public List<UITabView> getTabs() {
		return Views.fromObjects(uiObject.getForms(), UITabView.class, context);
	}
	
	public List<UITabComponentView<?>> getChildren() {
		return UITabComponentViews.fromObjects(uiObject.getChildren(), context);
	}
	
	public int getTotalColumns() {
		return uiObject.getTotalColumns();
	}
	
	public int getTotalRows() {
		return uiObject.getTotalRows();
	}
}