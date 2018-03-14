package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.NodeDefinitionSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.springframework.beans.factory.annotation.Autowired;

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
	private RecordSessionManager sessionManager;

	public List<SurveySummary> getSurveySummaries() {
		String lang = getActiveLanguageCode();
		User loggedUser = sessionManager.getLoggedUser();
		List<SurveySummary> summaries = surveyManager.getSurveySummaries(lang, loggedUser);
		return summaries;
	}
	
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

	public boolean isActiveSurveyRecordsLocked() {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		boolean result = survey != null && survey.isPublished() && surveyManager.isRecordValidationInProgress(survey.getId());
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
	
	protected RecordSessionManager getSessionManager() {
		return sessionManager;
	}

	protected SurveyManager getSurveyManager() {
		return surveyManager;
	}

}
