package org.openforis.collect.designer.form;

import org.openforis.collect.designer.model.NamedObject;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class FormObject<T> {

	public static NamedObject VERSION_EMPTY_SELECTION;
	
	{
		//init static variables
		String emptyOptionLabel = Labels.getLabel("global.empty_option");
		VERSION_EMPTY_SELECTION = new NamedObject(emptyOptionLabel);
	}
	
	
	
}
