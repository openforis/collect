package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.ui.UICodeField;
import org.openforis.collect.metamodel.ui.UIField;
import org.openforis.collect.metamodel.ui.UIFormComponent;
import org.openforis.collect.metamodel.ui.UIFormSection;
import org.openforis.collect.metamodel.ui.UITable;
import org.openforis.collect.metamodel.ui.UITextField;
import org.openforis.collect.metamodel.view.ViewContext;

public abstract class UITabComponentViews {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<UITabComponentView<?>> fromObjects(List<UIFormComponent> components, ViewContext context) {
		List<UITabComponentView<?>> views = new ArrayList<UITabComponentView<?>>(components.size());
		for (UIFormComponent c : components) {
			if (c instanceof UIField) {
				if (c instanceof UICodeField) {
					views.add(new UICodeFieldView((UICodeField) c, context));
				} else if (c instanceof UITextField) {
					views.add(new UITextFieldView((UITextField) c, context));
				} else {
					views.add(new UIFieldView((UIField) c, context));
				}
			} else if (c instanceof UIFormSection) {
				views.add(new UIFieldSetView((UIFormSection) c, context));
			} else {
				views.add(new UITableView((UITable) c, context));
			}
		}
		return views;
	}
}