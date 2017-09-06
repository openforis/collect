/**
 * 
 */
package org.openforis.collect.web.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.concurrency.ApplicationLockingJob;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.datacleansing.DataQueryExectutorTask.DataQueryExecutorError;
import org.openforis.commons.web.HttpResponses;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Worker.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("api/job")
public class CollectJobController extends BasicController {

	@Autowired
	private CollectJobManager jobManager;
	
	@RequestMapping(value = "application-job.json", method = RequestMethod.GET)
	public @ResponseBody JobView getApplicationJob(HttpServletResponse response) {
		ApplicationLockingJob job = jobManager.getApplicationJob();
		return createJobView(response, job);
	}
	
	@RequestMapping(value="application-job.json", method = RequestMethod.DELETE)
	public @ResponseBody
	JobView abortApplicationJob(HttpServletResponse response) {
		ApplicationLockingJob job = jobManager.getApplicationJob();
		return abortJob(response, job);
	}

	@RequestMapping(value = "survey-job.json", method = RequestMethod.GET)
	public @ResponseBody JobView getSurveyJob(HttpServletResponse response, @RequestParam("surveyId") int surveyId) {
		SurveyLockingJob job = jobManager.getSurveyJob(surveyId);
		return createJobView(response, job);
	}
	
	@RequestMapping(value="survey-job.json", method = RequestMethod.DELETE)
	public @ResponseBody
	JobView abortSurveyJob(HttpServletResponse response, @RequestParam("surveyId") int surveyId) {
		SurveyLockingJob job = jobManager.getSurveyJob(surveyId);
		return abortJob(response, job);
	}
	
	@RequestMapping(value="{jobId}", method = RequestMethod.GET)
	public @ResponseBody
	JobView getJob(HttpServletResponse response, @PathVariable("jobId") String jobId) {
		Job job = jobManager.getJob(jobId);
		return createJobView(response, job);
	}

	private JobView abortJob(HttpServletResponse response, Job job) {
		if (job != null) {
			job.abort();
		}
		return createJobView(response, job);
	}
	
	private JobView createJobView(HttpServletResponse response, Job job) {
		if (job == null) {
			HttpResponses.setNoContentStatus(response);
			return null;
		} else {
			return new JobView(job);
		}
	}

	public static class JobView {
		
		private String id;
		private String name;
		private int progressPercent;
		private Status status;
		private String errorMessage;
		private List<DataQueryExecutorError> errors;
		private long elapsedTime;
		private Long remainingTime;
		private Integer remainingMinutes;
		private boolean ended;

		public JobView(Job job) {
			id = job.getId().toString();
			name = job.getName();
			progressPercent = job.getProgressPercent();
			status = job.getStatus();
			ended = job.isEnded();
			errorMessage = job.getErrorMessage();
			elapsedTime = calculateElapsedTime(job);
			remainingTime = calculateRemainingTime();
			remainingMinutes = calculateRemainingMinutes();
		}

		private long calculateElapsedTime(Job job) {
			if (job.isEnded()) {
				return job.getEndTime() - job.getStartTime();
			} else {
				return new Date().getTime() - job.getStartTime();
			}
		}
		
		public Long calculateRemainingTime() {
			if (progressPercent <= 0) {
				return null;
			}
			long estimatedTotalTime = (100 * elapsedTime) / progressPercent;
			return estimatedTotalTime - elapsedTime;
		}
		
		public Integer calculateRemainingMinutes() {
			if (remainingTime == null) {
				return null;
			}
			return Double.valueOf(Math.ceil((double) remainingTime / 60000)).intValue();
		}
		
		public boolean isEnded() {
			return ended;
		}
		
		public String getId() {
			return id;
		}
		
		public String getName() {
			return name;
		}
		
		public int getProgressPercent() {
			return progressPercent;
		}
		
		public Status getStatus() {
			return status;
		}
		
		public String getErrorMessage() {
			return errorMessage;
		}
		
		public List<DataQueryExecutorError> getErrors() {
			return errors;
		}

		public long getElapsedTime() {
			return elapsedTime;
		}
		
		public Long getRemainingTime() {
			return remainingTime;
		}
		
		public Integer getRemainingMinutes() {
			return remainingMinutes;
		}
	}

}
