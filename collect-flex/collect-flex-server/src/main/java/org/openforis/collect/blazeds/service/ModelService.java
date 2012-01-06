package org.openforis.collect.blazeds.service;

import java.util.Collection;
import java.util.List;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.proxy.SurveyProxy;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.Survey;
import org.springframework.beans.factory.annotation.Autowired;

public class ModelService {

	@Autowired
	protected SurveyManager surveyManager;
	
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

	public List<Survey> getSurveys() {
		return null;
	}
	
	public SurveyProxy getSurvey(String name) {
		Survey survey = surveyManager.load(name);
		SurveyProxy proxy = new SurveyProxy(survey);
		return proxy;
	}
}
