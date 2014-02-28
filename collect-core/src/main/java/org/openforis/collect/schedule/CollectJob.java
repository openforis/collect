package org.openforis.collect.schedule;

import org.openforis.schedule.Job;
import org.openforis.schedule.Task;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class CollectJob<J extends CollectJob<J>> extends Job<J> {

	@Autowired
	private BeanFactory beanFactory;

	protected <T extends Task<J>> T createTask(Class<T> type) {
		T task = beanFactory.getBean(type);
		return task;
	}
	
}
