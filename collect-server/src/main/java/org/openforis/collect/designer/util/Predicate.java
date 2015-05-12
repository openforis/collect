package org.openforis.collect.designer.util;

/**
 * 
 * @author S. Ricci
 * 
 */
public interface Predicate<T> {
	
	public boolean evaluate(T item);
	
}