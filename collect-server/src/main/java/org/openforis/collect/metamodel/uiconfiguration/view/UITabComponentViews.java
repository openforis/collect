package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.ui.UIField;
import org.openforis.collect.metamodel.ui.UIFormComponent;
import org.openforis.collect.metamodel.ui.UIFormSection;
import org.openforis.collect.metamodel.ui.UITable;
import org.openforis.collect.metamodel.view.ViewContext;

public abstract class UITabComponentViews {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<UITabComponentView<?>> fromObjects(List<UIFormComponent> components, ViewContext context) {
		List<UITabComponentView<?>> views = new ArrayList<UITabComponentView<?>>(components.size());
		for (UIFormComponent c : components) {
			if (c instanceof UIField) {
				views.add(new UIFieldView((UIField) c, context));
			} else if (c instanceof UIFormSection) {
				views.add(new UIFieldSetView((UIFormSection) c, context));
			} else {
				views.add(new UITableView((UITable) c, context));
			}
		}
		return views;
	}
}