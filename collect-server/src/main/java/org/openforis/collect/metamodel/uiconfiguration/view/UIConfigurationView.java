package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.Collections;
import java.util.List;

import org.openforis.collect.metamodel.ui.UIConfiguration;

public class UIConfigurationView {

	private transient UIConfiguration uiConfiguration;

	public UIConfigurationView(UIConfiguration uiConfiguration) {
		super();
		this.uiConfiguration = uiConfiguration;
	}

	public List<UITabSetView> getTabSets() {
		if (uiConfiguration == null) {
			return Collections.emptyList();
		}
		return Views.fromObjects(uiConfiguration.getFormSets(), UITabSetView.class);
	}
	
}

