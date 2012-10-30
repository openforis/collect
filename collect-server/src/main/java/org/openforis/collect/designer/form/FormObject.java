package org.openforis.collect.designer.form;

import org.openforis.collect.designer.model.NamedObject;
import org.zkoss.util.resource.Labels;
import static org.openforis.collect.designer.model.LabelKeys.*;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class FormObject<T> {

	public static NamedObject VERSION_EMPTY_SELECTION;
	
	{
		//init static variables
		String emptyOptionLabel = Labels.getLabel(EMPTY_OPTION);
		VERSION_EMPTY_SELECTION = new NamedObject(emptyOptionLabel);
	}
	
	
	protected abstract void reset();
	
	
}
