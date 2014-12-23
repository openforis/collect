package org.openforis.collect.web.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.SurveyViewGenerator;
import org.openforis.collect.metamodel.SurveyViewGenerator.SurveyView;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.commons.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author S. Ricci
 * 
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/session")
public class SessionController {
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private SurveyManager surveyManager;
	
	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public @ResponseBody String keepSessionAlive(@RequestParam(value="editing", required = false, defaultValue = "false" ) Boolean editing) throws RecordUnlockedException {
		if ( editing ) {
			sessionManager.checkIsActiveRecordLocked();
		}
		return "ok";
	}
	
	@RequestMapping(value = "/survey.json", method = RequestMethod.POST)
	public @ResponseBody Response setActiveSurvey(@RequestParam int surveyId) {
		CollectSurvey survey = surveyManager.getById(surveyId);
		sessionManager.setActiveSurvey(survey);
		return new Response();
	}
	
	@RequestMapping(value = "/survey.json", method = RequestMethod.GET)
	public @ResponseBody SurveyView getActiveSurvey(HttpServletResponse response) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		Locale locale = sessionManager.getSessionState().getLocale();
		if (locale == null) {
			locale = Locale.ENGLISH;
		}
		SurveyViewGenerator viewGenerator = new SurveyViewGenerator(locale);
		if (survey == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null;
		} else {
			SurveyView view = viewGenerator.generateView(survey);
			return view;
		}
	}
	
}
