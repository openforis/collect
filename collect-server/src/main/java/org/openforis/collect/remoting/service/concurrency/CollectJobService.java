package org.openforis.collect.remoting.service.concurrency;

import org.openforis.collect.concurrency.ApplicationLockingJob;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.remoting.service.concurrency.proxy.ApplicationLockingJobProxy;
import org.openforis.collect.remoting.service.concurrency.proxy.SurveyLockingJobProxy;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.proxy.JobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CollectJobService {

	@Autowired
	private CollectJobManager jobManager;
	
	public JobProxy getJob(String lockId) {
		Job job = jobManager.getJob(lockId);
		return job == null ? null : new JobProxy(job);
	}
	
	public ApplicationLockingJobProxy getApplicationJob() {
		ApplicationLockingJob job = jobManager.getApplicationJob();
		return job == null ? null : new ApplicationLockingJobProxy(job);
	}
	
	public void abortApplicationJob() {
		Job job = jobManager.getApplicationJob();
		if (job != null) {
			job.abort();
		}
	}
	
	public SurveyLockingJobProxy getSurveyJob(int surveyId) {
		SurveyLockingJob job = jobManager.getSurveyJob(surveyId);
		return job == null ? null : new SurveyLockingJobProxy(job);
	}
	
	public void abortSurveyJob(int surveyId) {
		Job job = jobManager.getSurveyJob(surveyId);
		if (job != null) {
			job.abort();
		}
	}
	
}
