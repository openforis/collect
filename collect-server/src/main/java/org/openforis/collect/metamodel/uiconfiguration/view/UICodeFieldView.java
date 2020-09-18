package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.metamodel.ui.UICodeField;
import org.openforis.collect.metamodel.ui.UIOptions.CodeAttributeLayoutType;
import org.openforis.collect.metamodel.ui.UIOptions.Orientation;
import org.openforis.collect.metamodel.view.ViewContext;

public class UICodeFieldView extends UIFieldView<UICodeField> {

	public UICodeFieldView(UICodeField uiField, ViewContext context) {
		super(uiField, context);
	}
	
	public Integer getCodeListId() {
		return uiObject.getListId();
	}
	
	public Orientation getItemsOrientation() {
		return uiObject.getItemsOrientation();
	}
	
	public boolean isShowCode() {
		return uiObject.isShowCode();
	}
	
	public CodeAttributeLayoutType getLayout() {
		return uiObject.getLayout();
	}

}
