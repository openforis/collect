package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.metamodel.view.SurveyView;
import org.openforis.collect.metamodel.view.SurveyViewGenerator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.web.controller.UserController.UserForm;
import org.openforis.collect.web.session.SessionState;
import org.openforis.commons.web.HttpResponses;
import org.openforis.commons.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author S. Ricci
 * 
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("api/session")
public class SessionController extends BasicController {
	
	@Autowired
	private RecordSessionManager sessionManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private UserGroupManager userGroupManager;
	
	@RequestMapping(value = "ping", method = GET)
	public @ResponseBody Response ping(@RequestParam(value="editing", required = false, defaultValue = "false" ) Boolean editing) throws RecordUnlockedException {
		if ( editing ) {
			sessionManager.checkIsActiveRecordLocked();
		}
		return new Response();
	}
	
	@RequestMapping(value = "survey", method = POST)
	public @ResponseBody Response setActiveSurvey(@RequestParam int surveyId) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		sessionManager.setActiveSurvey(survey);
		return new Response();
	}
	
	@RequestMapping(value = "survey", method = GET)
	public @ResponseBody SurveyView getActiveSurvey(HttpServletResponse response) {
		CollectSurvey survey = getUpdatedActiveSurvey();
		if (survey == null) {
			HttpResponses.setNoContentStatus(response);
			return null;
		} else {
			SessionState sessionState = sessionManager.getSessionState();
			Locale locale = sessionState.getLocale();
			if (locale == null) {
				locale = Locale.ENGLISH;
			}
			SurveyViewGenerator viewGenerator = new SurveyViewGenerator(locale.getLanguage());
			
			UserInGroup userInSurveyGroup = userGroupManager.findUserInGroupOrDescendants(survey.getUserGroupId(), sessionState.getUser().getId());
			UserGroup userGroup = userInSurveyGroup == null ? null : userGroupManager.loadById(userInSurveyGroup.getGroupId());
			SurveyView view = viewGenerator.generateView(survey, userGroup, userInSurveyGroup == null ? null : userInSurveyGroup.getRole());
			return view;
		}
	}
	
	@RequestMapping(value="initialize", method=POST)
	public @ResponseBody UserForm initialize(HttpServletRequest request) {
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		sessionState.setLocale(request.getLocale());
		return new UserController.UserForm(user);
	}
	
	@RequestMapping(value="user", method=GET)
	public @ResponseBody UserForm getLoggedUser(HttpServletRequest request, HttpServletResponse response) {
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState == null ? null : sessionState.getUser();
		if (user == null) {
			HttpResponses.setNoContentStatus(response);
			return null;
		}
		if (sessionState.getLocale() == null) {
			sessionState.setLocale(request.getLocale());
		}
		return new UserController.UserForm(user);
	}
	
	@RequestMapping(value="invalidate", method=POST)
	public @ResponseBody Response invalidate(HttpServletRequest request) {
		sessionManager.invalidateSession();
		request.getSession().invalidate();
		return new Response();
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
