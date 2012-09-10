package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class FormObject<T> {

	public static ModelVersion VERSION_EMPTY_SELECTION;
	
	{
		//init static variables
		VERSION_EMPTY_SELECTION = new ModelVersion();
		VERSION_EMPTY_SELECTION.setId(-1);
		String emptyOptionLabel = Labels.getLabel("global.empty_option");
		VERSION_EMPTY_SELECTION.setName(emptyOptionLabel);
	}
	
}
