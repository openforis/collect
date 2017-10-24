package org.openforis.concurrency.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
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
	
	@ExternalizedProperty
	public String getId() {
		return job.getId().toString();
	}
	
	@ExternalizedProperty
	public Status getStatus() {
		return Status.valueOf(job.getStatus().name());
	}
	
	@ExternalizedProperty
	public boolean isPending() {
		return job.isPending();
	}

	@ExternalizedProperty
	public boolean isRunning() {
		return job.isRunning();
	}

	@ExternalizedProperty
	public boolean isFailed() {
		return job.isFailed();
	}

	@ExternalizedProperty
	public boolean isAborted() {
		return job.isAborted();
	}

	@ExternalizedProperty
	public boolean isCompleted() {
		return job.isCompleted();
	}

	@ExternalizedProperty
	public int getProgressPercent() {
		return job.getProgressPercent();
	}

	@ExternalizedProperty
	public String getErrorMessage() {
		return job.getErrorMessage();
	}

	@ExternalizedProperty
	public String[] getErrorMessageArgs() {
		return job.getErrorMessageArgs();
	}
	
	@ExternalizedProperty
	public String getName() {
		return job.getName();
	}
	
}
