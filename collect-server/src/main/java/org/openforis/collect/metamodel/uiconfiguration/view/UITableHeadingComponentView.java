package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.ui.UIColumn;
import org.openforis.collect.metamodel.ui.UIColumnGroup;
import org.openforis.collect.metamodel.ui.UITableHeadingComponent;

public abstract class UITableHeadingComponentView<O extends UITableHeadingComponent> extends UIModelObjectView<O> {

	public UITableHeadingComponentView(O uiObject) {
		super(uiObject);
	}

	public static List<UITableHeadingComponentView<?>> fromObjects(List<UITableHeadingComponent> components) {
		List<UITableHeadingComponentView<?>> views = new ArrayList<UITableHeadingComponentView<?>>(components.size());
		for (UITableHeadingComponent c : components) {
			if (c instanceof UIColumn) {
				views.add(new UIColumnView((UIColumn) c));
			} else {
				views.add(new UIColumnGroupView((UIColumnGroup) c));
			}
		}
		return views;
	}
}