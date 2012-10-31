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
	
	static {
		//init static variables
		VERSION_EMPTY_SELECTION = new NamedObject(EMPTY_OPTION);
	}
	
	public abstract void loadFrom(T source, String language, String defaultLanguage);
	
	public abstract void saveTo(T dest, String language);
	
	protected abstract void reset();
	
}
