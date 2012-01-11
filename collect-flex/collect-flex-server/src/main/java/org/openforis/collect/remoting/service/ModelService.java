package org.openforis.collect.remoting.service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.proxy.SurveyProxy;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.session.SessionState;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.Survey;
import org.springframework.beans.factory.annotation.Autowired;

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

	public SurveyProxy getSurvey(String name) {
		Survey survey = surveyManager.load(name);
		SurveyProxy proxy = new SurveyProxy(survey);
		return proxy;
	}

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
