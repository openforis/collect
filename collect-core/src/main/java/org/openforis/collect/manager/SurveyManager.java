/**
 * 
 */
package org.openforis.collect.manager;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyWorkDao;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class SurveyManager {

	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private SurveyDao surveyDao;
	@Autowired
	private SurveyWorkDao surveyWorkDao;
	@Autowired
	private CollectSurveyContext collectSurveyContext;
	
	private List<CollectSurvey> surveys;
	private Map<Integer, CollectSurvey> surveysById;
	private Map<String, CollectSurvey> surveysByName;
	private Map<String, CollectSurvey> surveysByUri;

	public SurveyManager() {
		surveysById = new HashMap<Integer, CollectSurvey>();
		surveysByName = new HashMap<String, CollectSurvey>();
		surveysByUri = new HashMap<String, CollectSurvey>();
	}

	@Transactional
	protected void init() {
		initSurveysCache();
	}

	protected void initSurveysCache() {
		surveysById.clear();
		surveysByName.clear();
		surveysByUri.clear();
		surveys = surveyDao.loadAll();
		for (CollectSurvey survey : surveys) {
			addToCache(survey);
		}
	}

	private void addToCache(CollectSurvey survey) {
		if ( ! surveys.contains(survey) ) {
			surveys.add(survey);
		}
		surveysById.put(survey.getId(), survey);
		surveysByName.put(survey.getName(), survey);
		surveysByUri.put(survey.getUri(), survey);
	}
	
	protected void removeFromCache(CollectSurvey survey) {
		surveys.remove(survey);
		surveysById.remove(survey.getId());
		surveysByName.remove(survey.getName());
		surveysByUri.remove(survey.getUri());
	}
	
	public List<CollectSurvey> getAll() {
		return CollectionUtils.unmodifiableList(surveys);
	}
	
	@Transactional
	public CollectSurvey get(String name) {
		CollectSurvey survey = surveysByName.get(name);
		return survey;
	}
	
	@Transactional
	public CollectSurvey getById(int id) {
		CollectSurvey survey = surveysById.get(id);
		return survey;
	}
	
	@Transactional
	public CollectSurvey getByUri(String uri) {
		CollectSurvey survey = surveysByUri.get(uri);
		return survey;
	}
	
	@Transactional
	public void importModel(CollectSurvey survey) throws SurveyImportException {
		surveyDao.importModel(survey);
		addToCache(survey);
	}
	
	@Transactional
	public void updateModel(CollectSurvey survey) throws SurveyImportException {
		//remove old survey from surveys cache
		CollectSurvey oldSurvey = surveysByName.get(survey.getName());
		if ( oldSurvey != null ) {
			removeFromCache(oldSurvey);
		} else {
			throw new SurveyImportException("Could not find survey to update");
		}
		surveyDao.updateModel(survey);
		addToCache(survey);
	}

	@Transactional
	public List<SurveySummary> getSurveySummaries(String lang) {
		List<SurveySummary> summaries = new ArrayList<SurveySummary>();
		for (Survey survey : surveys) {
			Integer id = survey.getId();
			String projectName = survey.getProjectName(lang);
			String name = survey.getName();
			String uri = survey.getUri();
			SurveySummary summary = new SurveySummary(id, name, uri, projectName);
			summaries.add(summary);
		}
		return summaries;
	}
	
	public String marshalSurvey(Survey survey)  {
		try {
			String result = surveyDao.marshalSurvey(survey);
			return result;
		} catch (SurveyImportException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void marshalSurvey(Survey survey, OutputStream os)  {
		try {
			surveyDao.marshalSurvey(survey, os);
		} catch (SurveyImportException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public CollectSurvey unmarshalSurvey(InputStream is) throws IdmlParseException {
		return surveyDao.unmarshalIdml(is);
	}
	
	public CollectSurvey unmarshalSurvey(Reader reader) throws IdmlParseException {
		return surveyDao.unmarshalIdml(reader);
	}
	
	@Transactional
	public List<SurveySummary> loadSurveyWorkSummaries(String lang) {
		List<SurveySummary> summaries = new ArrayList<SurveySummary>();
		for (Survey survey : surveys) {
			Integer id = survey.getId();
			String projectName = survey.getProjectName(lang);
			String name = survey.getName();
			SurveySummary summary = new SurveySummary(id, name, projectName);
			summaries.add(summary);
		}
		return summaries;
	}
	
	@Transactional
	public CollectSurvey loadSurveyWork(int id) {
		return surveyWorkDao.load(id);
	}
	
	@Transactional
	public List<SurveySummary> getSurveyWorkSummaries() {
		List<SurveySummary> result = surveyWorkDao.loadSummaries();
		return result;
	}
	
	@Transactional
	public SurveySummary loadSurveyWorkSummary(int id) {
		return surveyWorkDao.loadSurveySummary(id);
	}
	
	@Transactional
	public SurveySummary loadSurveyWorkSummaryByName(String name) {
		return surveyWorkDao.loadSurveySummaryByName(name);
	}
	
	@Transactional
	public CollectSurvey loadPublishedSurveyForEdit(String uri) {
		CollectSurvey surveyWork = surveyWorkDao.loadByUri(uri);
		if ( surveyWork == null ) {
			CollectSurvey publishedSurvey = (CollectSurvey) surveyDao.loadByUri(uri);
			surveyWork = createSurveyWork(publishedSurvey);
		}
		return surveyWork;
	}

	@Transactional
	public boolean isSurveyWork(CollectSurvey survey) {
		Integer id = survey.getId();
		String name = survey.getName();
		SurveySummary workSurveySummary = loadSurveyWorkSummaryByName(name);
		if (workSurveySummary == null || workSurveySummary.getId() != id ) {
			CollectSurvey publishedSurvey = get(name);
			if (publishedSurvey == null || publishedSurvey.getId() != id ) {
				throw new IllegalStateException("Survey with name '" + name
						+ "' not found");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	public CollectSurvey createSurveyWork() {
		CollectSurvey survey = (CollectSurvey) collectSurveyContext.createSurvey();
		UIOptions uiOptions = survey.createUIOptions();
		survey.addApplicationOptions(uiOptions);
		return survey;
	}
	
	protected CollectSurvey createSurveyWork(CollectSurvey survey) {
//		CollectSurvey surveyWork = survey.clone();
		CollectSurvey surveyWork = survey;
		surveyWork.setId(null);
		surveyWork.setPublished(true);
		return surveyWork;
	}
	
	@Transactional
	public void saveSurveyWork(CollectSurvey survey) throws SurveyImportException {
		Integer id = survey.getId();
		if ( id == null ) {
			surveyWorkDao.insert(survey);
			CollectSurvey publishedSurvey = surveyDao.loadByUri(survey.getUri());
			if ( publishedSurvey != null ) {
				int surveyWorkId = survey.getId();
				int publishedSurveyId = publishedSurvey.getId();
				samplingDesignManager.duplicateSamplingDesignForWork(publishedSurveyId, surveyWorkId);
				speciesManager.duplicateTaxonomyForWork(publishedSurveyId, surveyWorkId);
			}
		} else {
			surveyWorkDao.update(survey);
		}
	}
	
	@Transactional
	public void publish(CollectSurvey survey) throws SurveyImportException {
		Integer surveyWorkId = survey.getId();
		CollectSurvey publishedSurvey = get(survey.getName());
		if ( publishedSurvey == null ) {
			survey.setPublished(true);
			importModel(survey);
			initSurveysCache();
		} else {
			updateModel(survey);
		}
		if ( surveyWorkId != null ) {
			int publishedSurveyId = survey.getId();
			samplingDesignManager.publishSamplingDesign(surveyWorkId, publishedSurveyId);
			speciesManager.publishTaxonomies(surveyWorkId, publishedSurveyId);
			surveyWorkDao.delete(surveyWorkId);
		}
	}

	/*
	 * Getters and setters
	 * 
	 */
	public SamplingDesignManager getSamplingDesignManager() {
		return samplingDesignManager;
	}

	public void setSamplingDesignManager(SamplingDesignManager samplingDesignManager) {
		this.samplingDesignManager = samplingDesignManager;
	}

	public SpeciesManager getSpeciesManager() {
		return speciesManager;
	}

	public void setSpeciesManager(SpeciesManager speciesManager) {
		this.speciesManager = speciesManager;
	}

	public SurveyDao getSurveyDao() {
		return surveyDao;
	}

	public void setSurveyDao(SurveyDao surveyDao) {
		this.surveyDao = surveyDao;
	}

	public SurveyWorkDao getSurveyWorkDao() {
		return surveyWorkDao;
	}

	public void setSurveyWorkDao(SurveyWorkDao surveyWorkDao) {
		this.surveyWorkDao = surveyWorkDao;
	}

	public CollectSurveyContext getCollectSurveyContext() {
		return collectSurveyContext;
	}

	public void setCollectSurveyContext(CollectSurveyContext collectSurveyContext) {
		this.collectSurveyContext = collectSurveyContext;
	}
	
}
