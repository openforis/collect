package org.openforis.collect.datacleansing.controller;

import java.util.Collections;
import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingManager;
import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DataCleansingController {
	
	@Autowired
	private DataCleansingManager dataCleansingManager;
	@Autowired
	private SurveyManager surveyManager;
	
	@RequestMapping(method=RequestMethod.GET, value="/datacleansing/{surveyId}/errorqueries.json")
	public @ResponseBody List<DataErrorQuery> getErrorQueries(@PathVariable int surveyId) {
		CollectSurvey survey = surveyManager.getById(surveyId);
		List<DataErrorQuery> queries = dataCleansingManager.loadErrorQueriesBySurvey(survey);
		return queries;
	}

}
