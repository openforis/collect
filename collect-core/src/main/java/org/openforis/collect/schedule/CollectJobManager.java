package org.openforis.collect.schedule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executor;

import org.openforis.schedule.Job;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class CollectJobManager {
	
	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private Executor jobExecutor;
	
	private Collection<Integer> locks;

	private Map<Integer, Job<?>> jobByLockId;
	
	public CollectJobManager() {
		jobByLockId = new HashMap<Integer, Job<?>>();
		locks = new HashSet<Integer>();
	}
	
	public <J extends Job<J>> J createJob(Class<J> type) {
		J job = beanFactory.getBean(type);
		return job;
	}
	
	/**
	 * Executes a job in the background
	 */
	public <J extends CollectJob<J>> void startJob(final CollectJob<J> job) {
		startJob(job, null);
	}
	
	synchronized public <J extends CollectJob<J>> void startJob(final CollectJob<J> job, final Integer lockId) {
		job.init();
		if ( lockId != null ) {
			if ( locks.contains(lockId) ) {
				throw new RuntimeException("Another job is runnign for the same locking group: " + lockId);
			} else {
				locks.add(lockId);
				jobByLockId.put(lockId, job);
			}
		}
		jobExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					job.run();
				} finally {
					if ( lockId != null ) {
						locks.remove(lockId);
					}
				}
			}
		});
	}
}
