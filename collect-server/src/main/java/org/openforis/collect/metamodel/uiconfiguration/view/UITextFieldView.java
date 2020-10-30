package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.metamodel.ui.UITextField;
import org.openforis.collect.metamodel.ui.UITextField.TextTransform;
import org.openforis.collect.metamodel.view.ViewContext;

public class UITextFieldView extends UIFieldView<UITextField> {

	public UITextFieldView(UITextField uiField, ViewContext context) {
		super(uiField, context);
	}
	
	public TextTransform getTextTransform() {
		return uiObject.getTextTranform();
	}
}
