package org.openforis.collect.designer.form;

/**
 * 
 * @author S. Ricci
 *
 * @param <T>
 */
public abstract class ItemFormObject<T> extends FormObject<T> {
	
	public abstract void setValues(T source, String languageCode);
	
	public abstract void copyValues(T dest, String languageCode);
			
}
