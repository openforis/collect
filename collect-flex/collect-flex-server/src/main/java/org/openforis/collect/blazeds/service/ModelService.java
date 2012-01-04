package org.openforis.collect.blazeds.service;

import java.util.Collection;
import java.util.List;

import org.openforis.collect.persistence.SurveyDAO;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.Survey;
import org.springframework.beans.factory.annotation.Autowired;

public class ModelService {

	@Autowired
	protected SurveyDAO surveyDao;
	
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
	
	public Survey getSurvey(int id) {
		Survey survey = surveyDao.load(id);
		return survey;
	}
}
