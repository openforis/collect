/**
 * 
 */
package org.openforis.collect.context;

import org.springframework.context.ApplicationContext;

/**
 * @author M. Togna
 * 
 */
public class CollectContext {

	private static ApplicationContext applicationContext;

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	static void setApplicationContext(ApplicationContext springContext) {
		CollectContext.applicationContext = springContext;
	}
}
