package org.openforis.collect.web.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.view.SurveyView;
import org.openforis.collect.metamodel.view.SurveyViewGenerator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.web.controller.UserController.UserForm;
import org.openforis.collect.web.session.SessionState;
import org.openforis.commons.web.HttpResponses;
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
public class SessionController extends BasicController {
	
	@Autowired
	private RecordSessionManager sessionManager;
	@Autowired
	private SurveyManager surveyManager;
	
	@RequestMapping(value = "ping", method = RequestMethod.GET)
	public @ResponseBody Response ping(@RequestParam(value="editing", required = false, defaultValue = "false" ) Boolean editing) throws RecordUnlockedException {
		if ( editing ) {
			sessionManager.checkIsActiveRecordLocked();
		}
		return new Response();
	}
	
	@RequestMapping(value = "survey", method = RequestMethod.POST)
	public @ResponseBody Response setActiveSurvey(@RequestParam int surveyId) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		sessionManager.setActiveSurvey(survey);
		return new Response();
	}
	
	@RequestMapping(value = "survey", method = RequestMethod.GET)
	public @ResponseBody SurveyView getActiveSurvey(HttpServletResponse response) {
		CollectSurvey survey = getUpdatedActiveSurvey();
		if (survey == null) {
			HttpResponses.setNoContentStatus(response);
			return null;
		} else {
			Locale locale = sessionManager.getSessionState().getLocale();
			if (locale == null) {
				locale = Locale.ENGLISH;
			}
			SurveyViewGenerator viewGenerator = new SurveyViewGenerator(locale.getLanguage());
			SurveyView view = viewGenerator.generateView(survey);
			return view;
		}
	}
	
	@RequestMapping(value="user", method=RequestMethod.GET)
	public @ResponseBody UserForm getActiveUser(HttpServletRequest request, HttpServletResponse response) {
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		if (user == null) {
			HttpResponses.setNoContentStatus(response);
			return null;
		}
		return new UserController.UserForm(user);
	}
	
	private CollectSurvey getUpdatedActiveSurvey() {
		CollectSurvey sessionSurvey = sessionManager.getActiveSurvey();
		if (sessionSurvey == null) {
			return null;
		}
		CollectSurvey storedSurvey;
		if (sessionSurvey.isTemporary()) {
			storedSurvey = surveyManager.loadSurvey(sessionSurvey.getId());
		} else {
			storedSurvey = surveyManager.getById(sessionSurvey.getId());
		}
		if (storedSurvey == null || storedSurvey.isTemporary() != sessionSurvey.isTemporary()) {
			return null;
		} else if (storedSurvey.getModifiedDate().compareTo(sessionSurvey.getModifiedDate()) > 0) {
			//survey updated
			sessionManager.setActiveSurvey(storedSurvey);
			return storedSurvey;
		} else {
			return sessionSurvey;
		}
	}

}
