package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.Collections;
import java.util.List;

import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.view.ViewContext;

public class UIConfigurationView {

	private transient UIConfiguration uiConfiguration;
	private ViewContext context;

	public UIConfigurationView(UIConfiguration uiConfiguration, ViewContext context) {
		super();
		this.uiConfiguration = uiConfiguration;
		this.context = context;
	}

	public List<UITabSetView> getTabSets() {
		if (uiConfiguration == null) {
			return Collections.emptyList();
		}
		return Views.fromObjects(uiConfiguration.getFormSets(), UITabSetView.class, this.context);
	}
	
}

