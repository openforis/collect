package org.openforis.collect.application;

public interface CollectApplicationContext {
	
	<T> T getBean(Class<T> requiredType);
	
	<T> T getBean(String name, Class<T> requiredType);
	
}
