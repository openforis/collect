package org.openforis.collect.designer.form;

import static org.openforis.collect.designer.model.LabelKeys.EMPTY_OPTION;

import org.openforis.collect.designer.model.NamedObject;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class FormObject<T> {

	public static NamedObject VERSION_EMPTY_SELECTION;
	
	{
		//init static variables
		VERSION_EMPTY_SELECTION = new NamedObject(EMPTY_OPTION);
	}
	
	
	protected abstract void reset();
	
	
}
