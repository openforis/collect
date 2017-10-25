package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.sql.SQLException;

import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.reporting.ReportingRepositories;
import org.openforis.collect.reporting.ReportingRepositoriesGeneratorJob;
import org.openforis.collect.reporting.ReportingRepositoriesGeneratorJob.Input;
import org.openforis.collect.reporting.ReportingRepositoryInfo;
import org.openforis.commons.web.Response;
import org.openforis.concurrency.proxy.JobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("/api/saiku/")
public class SaikuController {

	@Autowired
	private ReportingRepositories reportingRepositories;
	@Autowired
	private CollectJobManager jobManager;
	@Autowired
	private SurveyManager surveyManager;
	
	@RequestMapping(value = "datasources/{surveyName}/generate", method=POST)
	public @ResponseBody JobProxy generateRepository(@PathVariable String surveyName, @RequestParam String language)
			throws CollectRdbException, SQLException {
		CollectSurvey survey = surveyManager.get(surveyName);
		ReportingRepositoriesGeneratorJob job = jobManager.createJob(ReportingRepositoriesGeneratorJob.class);
		job.setInput(new Input(language));
		job.setSurvey(survey);
		jobManager.startSurveyJob(job);
		return new JobProxy(job);
	}
	
	@RequestMapping(value = "datasources/{surveyName}/info", method=GET)
	public @ResponseBody Response getInfo(@PathVariable String surveyName) {
		ReportingRepositoryInfo info = reportingRepositories.getInfo(surveyName);
		Response response = new Response();
		response.setObject(info);
		return response;
	}
	
}
