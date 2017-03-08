package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.ui.UICodeField;
import org.openforis.collect.metamodel.ui.UIField;
import org.openforis.collect.metamodel.ui.UIFormComponent;
import org.openforis.collect.metamodel.ui.UIFormSection;
import org.openforis.collect.metamodel.ui.UITable;

public abstract class UITabComponentViews {

	public static List<UITabComponentView<?>> fromObjects(List<UIFormComponent> components) {
		List<UITabComponentView<?>> views = new ArrayList<UITabComponentView<?>>(components.size());
		for (UIFormComponent c : components) {
			if (c instanceof UIField) {
				if (c instanceof UICodeField) {
					views.add(new UICodeFieldView((UICodeField) c));
				} else {
					views.add(new UIFieldView((UIField) c));
				}
			} else if (c instanceof UIFormSection) {
				views.add(new UIFieldSetView((UIFormSection) c));
			} else {
				views.add(new UITableView((UITable) c));
			}
		}
		return views;
	}
}