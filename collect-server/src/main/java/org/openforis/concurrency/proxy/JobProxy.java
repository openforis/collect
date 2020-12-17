package org.openforis.concurrency.proxy;

import org.openforis.collect.Proxy;
import org.openforis.concurrency.Job;

/**
 * 
 * @author S. Ricci
 *
 */
public class JobProxy implements Proxy {
	
	protected transient Job job;

	public enum Status {
		PENDING, RUNNING, COMPLETED, FAILED, ABORTED;
	}
	
	public JobProxy(Job job) {
		super();
		this.job = job;
	}

	protected Job getJob() {
		return job;
	}
	
	public String getId() {
		return job.getId().toString();
	}
	
	public Status getStatus() {
		return Status.valueOf(job.getStatus().name());
	}
	
	public boolean isPending() {
		return job.isPending();
	}

	public boolean isRunning() {
		return job.isRunning();
	}

	public boolean isFailed() {
		return job.isFailed();
	}

	public boolean isAborted() {
		return job.isAborted();
	}

	public boolean isCompleted() {
		return job.isCompleted();
	}

	public int getProgressPercent() {
		return job.getProgressPercent();
	}

	public String getErrorMessage() {
		return job.getErrorMessage();
	}

	public String[] getErrorMessageArgs() {
		return job.getErrorMessageArgs();
	}
	
	public String getName() {
		return job.getName();
	}
	
}
