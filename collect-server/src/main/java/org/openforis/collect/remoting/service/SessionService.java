/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.manager.DatabaseVersionManager;
import org.openforis.collect.manager.DatabaseVersionNotCompatibleException;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.proxy.SurveyProxy;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.UserProxy;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class SessionService {

	//private static Logger LOG = Logger.getLogger(SessionService.class);
	@Autowired
	protected RecordSessionManager sessionManager;
	@Autowired
	protected SurveyManager surveyManager;
	@Autowired
	protected DatabaseVersionManager databaseVersionManager;

	/**
	 * Method used to keep the session alive
	 * @throws RecordUnlockedException 
	 */
	public void keepAlive(Boolean editing) throws RecordUnlockedException {
		sessionManager.keepSessionAlive();
		if(editing) {
			sessionManager.checkIsActiveRecordLocked();
		}
	}
	
	/**
	 * Set a locale (language, country) into the session state object
	 * 
	 * @return map with user, sessionId
	 * @throws DatabaseVersionNotCompatibleException 
	 */
	//@Secured("isAuthenticated()")
	public Map<String, Object> initSession(String locale) throws DatabaseVersionNotCompatibleException {
		databaseVersionManager.checkIsVersionCompatible();
		
		sessionManager.setLocale(locale);
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		UserProxy userProxy = new UserProxy(user);
		String sessionId = sessionState.getSessionId();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", userProxy);
		result.put("sessionId", sessionId);
		return result;
	}
	
	public SurveyProxy setActiveSurvey(String name) {
		CollectSurvey survey = surveyManager.get(name);
		return setActiveSurvey(survey, false);
	}

	public SurveyProxy setActiveSurvey(int surveyId) {
		CollectSurvey survey = surveyManager.getById(surveyId);
		return setActiveSurvey(survey, false);
	}

	public SurveyProxy setActivePreviewSurvey(int surveyId) {
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		return setActiveSurvey(survey, true);
	}

	public SurveyProxy setDesignerSurveyAsActive(int surveyId, boolean work) {
		CollectSurvey survey = sessionManager.getActiveDesignerSurvey();
		if ( survey == null ) {
			if ( work ) {
				survey = surveyManager.loadSurvey(surveyId);
			} else {
				survey = surveyManager.getById(surveyId);
			}
		}
		if ( survey == null ) {
			throw new IllegalArgumentException("Survey not found");
		}
		return setActiveSurvey(survey, work);
	}

	protected SurveyProxy setActiveSurvey(CollectSurvey survey, boolean work) {
		SessionState sessionState = sessionManager.getSessionState();
		sessionState.setActiveSurvey(survey);
		sessionState.setActiveSurveyWork(work);
		SurveyProxy proxy = new SurveyProxy(survey);
		return proxy;
	}
	
	//@Secured("isAuthenticated()")
	public void logout() {
		sessionManager.invalidateSession();
	}
	
}
