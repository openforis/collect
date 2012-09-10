package org.openforis.collect.designer.form;

/**
 * 
 * @author S. Ricci
 *
 * @param <T>
 */
public abstract class ItemFormObject<T> extends FormObject<T> {
	
	public abstract void loadFrom(T source, String languageCode);
	
	public abstract void saveTo(T dest, String languageCode);
	
}
