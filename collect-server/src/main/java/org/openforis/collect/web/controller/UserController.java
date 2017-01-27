package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("/user/")
public class UserController {
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private UserManager userManager;

	@RequestMapping(value = "{userId}/available_surveys/summaries.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<SurveySummary> loadSummariesByUser(@PathVariable int userId) {
		User user = userManager.loadById(userId);
		return surveyManager.getSurveySummaries(user);
	}
	
}
