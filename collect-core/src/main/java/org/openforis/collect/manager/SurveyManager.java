/**
 * 
 */
package org.openforis.collect.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.manager.exception.CodeListImportException;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyWorkDao;
import org.openforis.collect.utils.OpenForisIOUtils;
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
	private CodeListManager codeListManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private SurveyDao surveyDao;
	@Autowired
	private SurveyWorkDao surveyWorkDao;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private CollectSurveyContext collectSurveyContext;
	@Autowired
	private SurveyValidator surveyValidator;
	
	private List<CollectSurvey> surveys;
	private Map<Integer, CollectSurvey> surveysById;
	private Map<String, CollectSurvey> surveysByName;
	private Map<String, CollectSurvey> surveysByUri;

	public SurveyManager() {
		surveys = new ArrayList<CollectSurvey>();
		surveysById = new HashMap<Integer, CollectSurvey>();
		surveysByName = new HashMap<String, CollectSurvey>();
		surveysByUri = new HashMap<String, CollectSurvey>();
	}

	@Transactional
	public void init() {
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
	public CollectSurvey importWorkModel(InputStream is, String name, boolean validate)
			throws SurveyImportException, SurveyValidationException {
		File tempFile = null;
		try {
			tempFile = OpenForisIOUtils.copyToTempFile(is);
			return importWorkModel(tempFile, name, validate);
		} finally {
			if ( tempFile != null && tempFile.exists() ) {
				tempFile.delete();
			}
		}
	}

	@Transactional
	public CollectSurvey importWorkModel(File surveyFile, String name, boolean validate) throws SurveyImportException, SurveyValidationException {
		try {
			CollectSurvey survey = unmarshalSurvey(surveyFile, validate, false);
			survey.setName(name);
			survey.setWork(true);
			surveyWorkDao.insert(survey);
			codeListManager.importCodeLists(survey, surveyFile);
			return survey;
		} catch ( CodeListImportException e ) {
			throw new SurveyImportException(e);
		} catch (IdmlParseException e) {
			throw new SurveyImportException(e);
		}
	}

	@Transactional
	public CollectSurvey importInPublishedWorkModel(String uri, File surveyFile, boolean validate) throws SurveyImportException, SurveyValidationException {
		CollectSurvey surveyWork = duplicatePublishedSurveyForEdit(uri);
		updateSurveyWork(surveyFile, surveyWork);
		return surveyWork;
	}
	
	@Transactional
	public CollectSurvey importModel(InputStream is, String name, boolean validate)
			throws SurveyImportException, SurveyValidationException {
		File tempFile = null;
		try {
			tempFile = OpenForisIOUtils.copyToTempFile(is);
			return importModel(tempFile, name, validate);
		} finally {
			if ( tempFile != null && tempFile.exists() ) {
				tempFile.delete();
			}
		}
	}

	@Transactional
	public CollectSurvey importModel(File surveyFile, String name, boolean validate) throws SurveyImportException, SurveyValidationException {
		try {
			CollectSurvey survey = unmarshalSurvey(surveyFile, validate, false);
			survey.setName(name);
			surveyDao.importModel(survey);
			addToCache(survey);
			codeListManager.importCodeLists(survey, surveyFile);
			return survey;
		} catch ( CodeListImportException e ) {
			throw new SurveyImportException(e);
		} catch (IdmlParseException e) {
			throw new SurveyImportException(e);
		}
	}

	@Transactional
	public CollectSurvey updateModel(InputStream is, boolean validate) throws IdmlParseException, SurveyValidationException, SurveyImportException {
		File tempFile = OpenForisIOUtils.copyToTempFile(is);
		try {
			return updateModel(tempFile, validate);
		} finally {
			tempFile.delete();
		}
	}

	@Transactional
	public CollectSurvey updateModel(File surveyFile, boolean validate)
			throws SurveyValidationException, SurveyImportException {
		CollectSurvey parsedSurvey;
		try {
			parsedSurvey = unmarshalSurvey(surveyFile, validate, false);
		} catch (IdmlParseException e) {
			throw new SurveyImportException(e);
		}
		String uri = parsedSurvey.getUri();
		SurveySummary oldSurveyWork = loadWorkSummaryByUri(uri);
		CollectSurvey oldPublishedSurvey = getByUri(uri);
		if ( oldSurveyWork == null && oldPublishedSurvey == null ) {
			throw new IllegalArgumentException("Survey to update not found: " + uri);
		} else if ( oldSurveyWork != null ) {
			updateSurveyWork(surveyFile, parsedSurvey, oldSurveyWork);
		} else {
			updatePublishedSurvey(surveyFile, parsedSurvey, validate);
		}
		return parsedSurvey;
	}
	
	protected void updateSurveyWork(File surveyFile,
			CollectSurvey parsedSurvey) throws SurveyImportException {
		SurveySummary oldSurveyWork = loadWorkSummaryByUri(parsedSurvey.getUri());
		updateSurveyWork(surveyFile, parsedSurvey, oldSurveyWork);
	}

	protected void updateSurveyWork(File surveyFile,
			CollectSurvey parsedSurvey, SurveySummary oldSurveyWorkSummary)
			throws SurveyImportException {
		Integer id = oldSurveyWorkSummary.getId();
		parsedSurvey.setId(id);
		parsedSurvey.setName(oldSurveyWorkSummary.getName());
		parsedSurvey.setWork(true);
		codeListManager.deleteAllItemsBySurvey(id, true);
		saveSurveyWork(parsedSurvey);
		try {
			codeListManager.importCodeLists(parsedSurvey, surveyFile);
		} catch (CodeListImportException e) {
			throw new SurveyImportException(e);
		}
	}

	protected void updatePublishedSurvey(File surveyFile,
			CollectSurvey survey, boolean validate) throws SurveyValidationException,
			SurveyImportException {
		CollectSurvey oldPublishedSurvey = getByUri(survey.getUri());
		Integer id = oldPublishedSurvey.getId();
		survey.setId(id);
		survey.setName(oldPublishedSurvey.getName());
		if ( validate ) {
			surveyValidator.checkCompatibility(oldPublishedSurvey, survey);
		}
		codeListManager.deleteAllItemsBySurvey(id, false);
		updateModel(survey);
		try {
			codeListManager.importCodeLists(survey, surveyFile);
		} catch (CodeListImportException e) {
			throw new SurveyImportException(e);
		}
	}

	@Transactional
	@Deprecated
	public void importModel(CollectSurvey survey) throws SurveyImportException {
		surveyDao.importModel(survey);
		addToCache(survey);
	}
	
	@Deprecated
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
		for (CollectSurvey survey : surveys) {
			SurveySummary summary = SurveySummary.createFromSurvey(survey, lang);
			summaries.add(summary);
		}
		sortByName(summaries);
		return summaries;
	}
	
	public SurveySummary getPublishedSummaryByUri(String uri) {
		CollectSurvey survey = getByUri(uri);
		if ( survey == null ) {
			return null;
		} else {
			return SurveySummary.createFromSurvey(survey);
		}
	}

	protected void sortByName(List<SurveySummary> summaries) {
		Collections.sort(summaries, new Comparator<SurveySummary>() {
			@Override
			public int compare(SurveySummary s1, SurveySummary s2) {
				return s1.getName().compareTo(s2.getName());
			}
		});
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
		marshalSurvey(survey, os, true, false, false);
	}
	
	public void marshalSurvey(Survey survey, OutputStream os,
			boolean marshalCodeLists, boolean marshalPersistedCodeLists,
			boolean marshalExternalCodeLists) {
		try {
			surveyDao.marshalSurvey(survey, os, marshalCodeLists,
					marshalPersistedCodeLists, marshalExternalCodeLists);
		} catch (SurveyImportException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public CollectSurvey unmarshalSurvey(InputStream is) throws IdmlParseException, SurveyValidationException {
		return unmarshalSurvey(is, false, true);
	}
	
	public CollectSurvey unmarshalSurvey(File surveyFile, boolean validate,
			boolean includeCodeListItems) throws IdmlParseException, SurveyValidationException {
		try {
			return unmarshalSurvey(new FileInputStream(surveyFile), validate, includeCodeListItems);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public CollectSurvey unmarshalSurvey(InputStream is,
			boolean validate, boolean includeCodeListItems)
			throws IdmlParseException, SurveyValidationException {
		return unmarshalSurvey(OpenForisIOUtils.toReader(is), validate, includeCodeListItems);
	}

	public CollectSurvey unmarshalSurvey(Reader reader) throws IdmlParseException, SurveyValidationException {
		return unmarshalSurvey(reader, false, true);
	}
	
	public CollectSurvey unmarshalSurvey(Reader reader,
			boolean validate, boolean includeCodeListItems)
			throws IdmlParseException, SurveyValidationException {
		CollectSurvey survey;
		File tempFile = OpenForisIOUtils.copyToTempFile(reader);
		if ( validate ) {
			//validate against schema
			validateSurveyXMLAgainstSchema(tempFile);
		}
		survey = unmarshalSurvey(tempFile, includeCodeListItems);
		if ( validate ) {
			surveyValidator.validate(survey);
		}
		tempFile.delete();
		return survey;
	}

	protected CollectSurvey unmarshalSurvey(File file, boolean includeCodeListItems) throws IdmlParseException {
		FileInputStream tempIs = null;
		try {
			tempIs = new FileInputStream(file);
			return surveyDao.unmarshalIdml(tempIs, includeCodeListItems);
		} catch (Exception e) {
			//should never enter here
			throw new RuntimeException(e); 
		} finally {
			IOUtils.closeQuietly(tempIs);
		}
	}

	protected void validateSurveyXMLAgainstSchema(File file) throws SurveyValidationException {
		surveyValidator.validateAgainstSchema(file);
	}

	@Transactional
	public List<SurveySummary> loadSummaries() {
		List<SurveySummary> surveySummaries = getSurveySummaries(null);
		List<SurveySummary> surveyWorkSummaries = loadWorkSummaries();
		List<SurveySummary> result = new ArrayList<SurveySummary>();
		Map<String, SurveySummary> summariesByUri = new HashMap<String, SurveySummary>();
		for (SurveySummary summary : surveyWorkSummaries) {
			summary.setPublished(false);
			summary.setWork(true);
			result.add(summary);
			summariesByUri.put(summary.getUri(), summary);
		}
		for (SurveySummary summary : surveySummaries) {
			SurveySummary summaryWork = summariesByUri.get(summary.getUri());
			if ( summaryWork == null ) {
				result.add(summary);
			} else {
				summaryWork.setPublished(true);
				summaryWork.setPublishedId(summary.getId());
			}
		}
		sortByName(result);
		return result;
	}
	
	@Transactional
	public SurveySummary loadSummaryByUri(String uri) {
		SurveySummary workSummary = loadWorkSummaryByUri(uri);
		SurveySummary publishedSummary = getPublishedSummaryByUri(uri);
		SurveySummary result; 
		if ( workSummary != null ) {
			result = workSummary;
			if ( publishedSummary != null ) {
				result.setPublished(true);
				result.setPublishedId(publishedSummary.getId());
			}
		} else {
			result = publishedSummary;
		}
		return result;
	}
	
	@Transactional
	public CollectSurvey loadSurveyWork(int id) {
		return surveyWorkDao.load(id);
	}
	
	@Transactional
	protected List<SurveySummary> loadWorkSummaries() {
		List<SurveySummary> result = surveyWorkDao.loadSummaries();
		return result;
	}
	
	@Transactional
	public SurveySummary loadWorkSummaryByUri(String uri) {
		return surveyWorkDao.loadSurveySummaryByUri(uri);
	}
	
	@Transactional
	public boolean isSurveyWork(CollectSurvey survey) {
		Integer id = survey.getId();
		String uri = survey.getUri();
		SurveySummary workSurveySummary = loadWorkSummaryByUri(uri);
		if (workSurveySummary == null || ! workSurveySummary.getId().equals(id) ) {
			CollectSurvey publishedSurvey = getByUri(uri);
			if (publishedSurvey == null || ! publishedSurvey.getId().equals(id) ) {
				throw new IllegalStateException("Survey with uri '" + uri
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
	
	protected CollectSurvey duplicatePublishedSurveyAsWork(String uri) {
		CollectSurvey survey = surveyDao.loadByUri(uri);
//		CollectSurvey surveyWork = survey.clone();
		CollectSurvey surveyWork = survey;
		surveyWork.setId(null);
		surveyWork.setPublished(true);
		surveyWork.setWork(true);
		try {
			surveyWorkDao.insert(surveyWork);
		} catch (SurveyImportException e) {
			//it should never enter here, we are duplicating an already existing survey
			throw new RuntimeException(e);
		}
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
	public CollectSurvey duplicatePublishedSurveyForEdit(String uri) {
		SurveySummary existingSurveyWork = surveyWorkDao.loadSurveySummaryByUri(uri);
		if ( existingSurveyWork != null ) {
			throw new IllegalArgumentException("Survey work already existing");
		}
		CollectSurvey surveyWork = duplicatePublishedSurveyAsWork(uri);
		CollectSurvey publishedSurvey = getByUri(uri);
		int surveyWorkId = surveyWork.getId();
		int publishedSurveyId = publishedSurvey.getId();
		samplingDesignManager.duplicateSamplingDesignForWork(publishedSurveyId, surveyWorkId);
		speciesManager.duplicateTaxonomyForWork(publishedSurveyId, surveyWorkId);
		codeListManager.cloneCodeLists(publishedSurvey, surveyWork);
		return surveyWork;
	}
	
	@Transactional
	public void publish(CollectSurvey survey) throws SurveyImportException {
		Integer surveyWorkId = survey.getId();
		CollectSurvey publishedSurvey = get(survey.getName());
		survey.setWork(false);
		survey.setPublished(true);
		if ( publishedSurvey == null ) {
			surveyDao.importModel(survey);
		} else {
			surveyDao.updateModel(survey);
		}
		int publishedSurveyId = survey.getId();
		samplingDesignManager.publishSamplingDesign(surveyWorkId, publishedSurveyId);
		speciesManager.publishTaxonomies(surveyWorkId, publishedSurveyId);
		codeListManager.publishCodeLists(surveyWorkId, publishedSurveyId);
		surveyWorkDao.delete(surveyWorkId);
		if ( publishedSurvey != null ) {
			removeFromCache(publishedSurvey);
		}
		addToCache(survey);
	}
	
	@Transactional
	public void deleteSurvey(Integer id) {
		CollectSurvey survey = getById(id);
		if ( survey != null ) {
			recordDao.deleteBySurvey(id);
			speciesManager.deleteTaxonomiesBySurvey(id);
			samplingDesignManager.deleteBySurvey(id);
			codeListManager.deleteAllItemsBySurvey(id, false);
			surveyDao.delete(id);
			removeFromCache(survey);
		}
	}
	
	@Transactional
	public void deleteSurveyWork(Integer id) {
		speciesManager.deleteTaxonomiesBySurveyWork(id);
		samplingDesignManager.deleteBySurveyWork(id);
		codeListManager.deleteAllItemsBySurvey(id, true);
		surveyWorkDao.delete(id);
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

	public CodeListManager getCodeListManager() {
		return codeListManager;
	}
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public SurveyValidator getSurveyValidator() {
		return surveyValidator;
	}
	
	public void setSurveyValidator(SurveyValidator validator) {
		this.surveyValidator = validator;
	}
	
}
