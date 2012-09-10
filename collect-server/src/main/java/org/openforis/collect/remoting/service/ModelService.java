package org.openforis.collect.remoting.service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.proxy.SurveyProxy;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author M. Togna
 *
 */
public class ModelService {

	@Autowired
	private SurveyManager surveyManager;

	@Autowired
	private SessionManager sessionManager;

	/**
		 */
	public Collection<SpatialReferenceSystem> getSpatialReferenceSystems() {
		return null;
	}

	/**
			 */
	public Collection<ModelVersion> getModelVersions() {
		return null;
	}
	
	@Transactional
	public SurveyProxy setActiveSurvey(String name) {
		CollectSurvey survey = surveyManager.get(name);
		SessionState sessionState = sessionManager.getSessionState();
		sessionState.setActiveSurvey(survey);
		SurveyProxy proxy = new SurveyProxy(survey);
		return proxy;
	}
	
	@Transactional
	public List<SurveySummary> getSurveySummaries() {
		SessionState sessionState = sessionManager.getSessionState();
		Locale locale = sessionState.getLocale();
		String lang = "en";
		if (locale != null) {
			lang = locale.getLanguage();
		}
		List<SurveySummary> summaries = surveyManager.getSurveySummaries(lang);
		return summaries;
	}

	protected SessionManager getSessionManager() {
		return sessionManager;
	}

	protected SurveyManager getSurveyManager() {
		return surveyManager;
	}

}
