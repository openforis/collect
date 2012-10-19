/**
 * 
 */
package org.openforis.collect.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyWorkDao;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class SurveyManager {

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
			initSurvey(survey);
		}
	}

	private void initSurvey(CollectSurvey survey) {
		surveysById.put(survey.getId(), survey);
		surveysByName.put(survey.getName(), survey);
		surveysByUri.put(survey.getUri(), survey);
	}
	
	public List<CollectSurvey> getAll() {
		return CollectionUtil.unmodifiableList(surveys);
	}
	
	@Transactional
	public CollectSurvey get(String name) {
		CollectSurvey survey = surveysByName.get(name);
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
		surveys.add(survey);
		initSurvey(survey);
	}
	
	@Transactional
	public void updateModel(CollectSurvey survey) throws SurveyImportException {
		//remove old survey from surveys cache
		String name = survey.getName();
		Iterator<CollectSurvey> iterator = surveys.iterator();
		while ( iterator.hasNext() ) {
			CollectSurvey oldSurvey = iterator.next();
			if (oldSurvey.getName().equals(name)) {
				iterator.remove();
				break;
			}
		}
		surveyDao.updateModel(survey);
		surveys.add(survey);
		initSurvey(survey);
	}

	@Transactional
	public List<SurveySummary> getSurveySummaries(String lang) {
		List<SurveySummary> summaries = new ArrayList<SurveySummary>();
		for (Survey survey : surveys) {
			Integer id = survey.getId();
			String projectName = getProjectName(survey, lang);
			String name = survey.getName();
			String uri = survey.getUri();
			SurveySummary summary = new SurveySummary(id, name, uri, projectName);
			summaries.add(summary);
		}
		return summaries;
	}
	
	@Transactional
	public List<SurveySummary> getSurveyWorkSummaries() {
		List<SurveySummary> result = surveyWorkDao.loadSummaries();
		return result;
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

	public CollectSurvey unmarshalSurvey(InputStream is) throws InvalidIdmlException {
		try {
			byte[] bytes = IOUtils.toByteArray(is);
			surveyDao.validateAgainstSchema(bytes);
			String idml = new String(bytes, "UTF-8");
			return surveyDao.unmarshalIdml(idml);
		} catch (IOException e) {
			throw new InvalidIdmlException("Error reading input stream");
		}
	}
	
	@Transactional
	public List<SurveySummary> loadSurveyWorkSummaries(String lang) {
		List<SurveySummary> summaries = new ArrayList<SurveySummary>();
		for (Survey survey : surveys) {
			Integer id = survey.getId();
			String projectName = getProjectName(survey, lang);
			String name = survey.getName();
			SurveySummary summary = new SurveySummary(id, name, projectName);
			summaries.add(summary);
		}
		return summaries;
	}
	
	@Transactional
	public CollectSurvey loadSurveyWork(int id) {
		CollectSurvey survey = surveyWorkDao.load(id);
		return survey;
	}
	
	@Transactional
	public CollectSurvey loadPublishedSurveyForEdit(String uri) {
		CollectSurvey survey = (CollectSurvey) surveyDao.loadByUri(uri);
		CollectSurvey tempSurvey = surveyWorkDao.loadByUri(uri);
		if ( tempSurvey != null ) {
			return tempSurvey;
		} else {
			CollectSurvey surveyWork = createSurveyWork(survey);
			return surveyWork;
		}
	}

	public CollectSurvey createSurveyWork() {
		CollectSurvey survey = (CollectSurvey) collectSurveyContext.createSurvey();
		UIOptions uiOptions = new UIOptions();
		survey.addApplicationOptions(uiOptions);
		return survey;
	}
	
	protected CollectSurvey createSurveyWork(CollectSurvey survey) {
//		CollectSurvey surveyWork = survey.clone();
		CollectSurvey surveyWork = survey;
		surveyWork.setId(null);
		return surveyWork;
	}
	
	@Transactional
	public void saveSurveyWork(CollectSurvey survey) throws SurveyImportException {
		Integer id = survey.getId();
		if ( id == null ) {
			surveyWorkDao.insert(survey);
		} else {
			surveyWorkDao.update(survey);
		}
	}
	
	@Transactional
	public void publish(CollectSurvey survey) throws SurveyImportException {
		Integer surveyWorkId = survey.getId();
		if ( survey.isPublished() ) {
			updateModel(survey);
		} else {
			survey.setPublished(true);
			importModel(survey);
			initSurveysCache();
		}
		if ( surveyWorkId != null ) {
			surveyWorkDao.delete(surveyWorkId);
		}
	}
	
	private String getProjectName(Survey survey, String lang) {
		List<LanguageSpecificText> names = survey.getProjectNames();
		if (names == null || names.size() == 0) {
			return "";
		} else if (names.size() == 1) {
			return names.get(0).getText();
		} else {
			for (LanguageSpecificText text : names) {
				if (lang.equalsIgnoreCase(text.getLanguage())) {
					return text.getText();
				}
			}
		}
		return "";
	}

}
