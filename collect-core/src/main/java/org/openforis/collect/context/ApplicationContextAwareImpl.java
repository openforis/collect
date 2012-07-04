/**
 * 
 */
package org.openforis.collect.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author M. Togna
 *
 */
public class ApplicationContextAwareImpl implements ApplicationContextAware {
	
	public ApplicationContextAwareImpl() {
		super();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		CollectContext.setApplicationContext(applicationContext);
	}

}
