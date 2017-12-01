package org.openforis.collect.metamodel.uiconfiguration.view;

import org.openforis.collect.metamodel.ui.UIFormComponent;

public interface UITabComponentView<O extends UIFormComponent> {
	
	int getColumn();
	
	int getColumnSpan();
	
	int getRow();
	
}