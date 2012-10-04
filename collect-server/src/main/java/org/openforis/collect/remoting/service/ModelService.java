package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.NodeDefinitionSummary;
import org.openforis.collect.metamodel.proxy.SurveyProxy;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
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
		String lang = getActiveLanguageCode();
		List<SurveySummary> summaries = surveyManager.getSurveySummaries(lang);
		return summaries;
	}
	
	@Transactional
	public List<NodeDefinitionSummary> getRootEntitiesSummaries(String surveyName) {
		String lang = getActiveLanguageCode();
		List<NodeDefinitionSummary> result = new ArrayList<NodeDefinitionSummary>();
		CollectSurvey survey = surveyManager.get(surveyName);
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition entityDefinition : rootEntityDefinitions) {
			Integer id = entityDefinition.getId();
			String name = entityDefinition.getName();
			String label = entityDefinition.getLabel(Type.HEADING, lang);
			NodeDefinitionSummary summary = new NodeDefinitionSummary(id, name, label);
			result.add(summary);
		}
		return result;
	}

	protected String getActiveLanguageCode() {
		SessionState sessionState = sessionManager.getSessionState();
		Locale locale = sessionState.getLocale();
		String lang;
		if (locale != null) {
			lang = locale.getLanguage();
		} else {
			lang = "en";
		}
		return lang;
	}
	
	protected SessionManager getSessionManager() {
		return sessionManager;
	}

	protected SurveyManager getSurveyManager() {
		return surveyManager;
	}

}
