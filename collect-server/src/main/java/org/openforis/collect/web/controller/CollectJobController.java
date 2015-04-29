/**
 * 
 */
package org.openforis.collect.web.controller;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.concurrency.ApplicationLockingJob;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Worker.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 *
 */
@Controller
public class CollectJobController extends BasicController {

	@Autowired
	private CollectJobManager jobManager;
	
	@RequestMapping(value = "application-job.json", method = RequestMethod.GET)
	public @ResponseBody JobView getApplicationJob(HttpServletResponse response) {
		ApplicationLockingJob job = jobManager.getApplicationJob();
		return createJobView(response, job);
	}
	
	@RequestMapping(value = "survey-job.json", method = RequestMethod.GET)
	public @ResponseBody JobView getSurveyJob(HttpServletResponse response, @RequestParam("surveyId") int surveyId) {
		SurveyLockingJob job = jobManager.getSurveyJob(surveyId);
		return createJobView(response, job);
	}
	
	private JobView createJobView(HttpServletResponse response, Job job) {
		if (job == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null;
		} else {
			return new JobView(job);
		}
	}

	public static class JobView {
		
		private int progressPercent;
		private Status status;
		private String errorMessage;

		public JobView(Job job) {
			progressPercent = job.getProgressPercent();
			status = job.getStatus();
			errorMessage = job.getErrorMessage();
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
		
	}
	
}
