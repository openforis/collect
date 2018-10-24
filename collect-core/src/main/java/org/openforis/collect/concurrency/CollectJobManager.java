package org.openforis.collect.concurrency;

import org.openforis.concurrency.Job;
import org.openforis.concurrency.spring.SpringJobManager;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectJobManager extends SpringJobManager {

	private static final String APP_LOCK_ID = "app";
	private static final String SURVEY_LOCK_ID_PREFIX = "survey_";

	public void startApplicationJob(Job job) {
		start(job, APP_LOCK_ID);
	}
	
	public ApplicationLockingJob getApplicationJob() {
		return (ApplicationLockingJob) getLockingJob(APP_LOCK_ID);
	}
	
	public void startSurveyJob(SurveyLockingJob job) {
		startSurveyJob(job, true);
	}
	
	public void startSurveyJob(SurveyLockingJob job, boolean async) {
		start(job, getLockId(job.getSurvey().getId()), async);
	}
	
	public SurveyLockingJob getSurveyJob(int surveyId) {
		return (SurveyLockingJob) getLockingJob(getLockId(surveyId));
	}
	
	private String getLockId(int surveyId) {
		return SURVEY_LOCK_ID_PREFIX + surveyId;
	}
	
}
