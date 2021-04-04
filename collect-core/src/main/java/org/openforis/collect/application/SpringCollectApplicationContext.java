package org.openforis.collect.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class SpringCollectApplicationContext implements CollectApplicationContext {

	@Autowired
	private ApplicationContext springApplicationContext;

	@Override
	public <T> T getBean(Class<T> requiredType) {
		return springApplicationContext.getBean(requiredType);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) {
		return springApplicationContext.getBean(name, requiredType);
	}

}
