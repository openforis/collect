/**
 * 
 */
package org.openforis.collect.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.io.exception.CodeListImportException;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.collect.manager.validation.RecordValidationProcess;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyWorkDao;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.utils.ExecutorServiceUtil;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class SurveyManager {
	
	private static Log LOG = LogFactory.getLog(SurveyManager.class);
	private static final String URI_PREFIX = "http://www.openforis.org/idm/";
	
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
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	protected CollectSurveyIdmlBinder surveySerializer;
	
	private List<CollectSurvey> surveys;
	private Map<Integer, CollectSurvey> surveysById;
	private Map<String, CollectSurvey> surveysByName;
	private Map<String, CollectSurvey> surveysByUri;
	
	private Map<Integer, ProcessStatus> recordValidationStatusBySurvey;
	
	public SurveyManager() {
		surveys = new ArrayList<CollectSurvey>();
		surveysById = new HashMap<Integer, CollectSurvey>();
		surveysByName = new HashMap<String, CollectSurvey>();
		surveysByUri = new HashMap<String, CollectSurvey>();
		recordValidationStatusBySurvey = Collections.synchronizedMap(new HashMap<Integer, ProcessStatus>());
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

	/**
	 * Duplicates a published survey into a work survey and import the survey file into this new survey work
	 */
	@Transactional
	public CollectSurvey importInPublishedWorkModel(String uri, File surveyFile, boolean validate) throws SurveyImportException, SurveyValidationException {
		duplicatePublishedSurveyForEdit(uri);
		CollectSurvey newSurveyWork = updateWorkModel(surveyFile, validate);
		return newSurveyWork;
	}
	
	/**
	 * Imports a survey from a XML file input stream and publishes it.
	 */
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
			survey.setPublished(true);
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

	/**
	 * Updates a published or a work survey and overwrites it with the specified one.
	 * The existing survey will be searched by his URI.
	 * If a work survey with the same URI as the survey in the surveyFile exists,
	 * than it will be overwritten with the passed one, otherwise the published survey will be overwritten.
	 */
	@Transactional
	public CollectSurvey updateModel(File surveyFile, boolean validate)
			throws SurveyValidationException, SurveyImportException {
		CollectSurvey parsedSurvey;
		try {
			parsedSurvey = unmarshalSurvey(surveyFile, validate, false);
		} catch (IdmlParseException e) {
			throw new SurveyImportException(e);
		}
		updateModel(surveyFile, parsedSurvey);
		/*
		String uri = parsedSurvey.getUri();
		SurveySummary oldSurveyWork = loadWorkSummaryByUri(uri);
		CollectSurvey oldPublishedSurvey = getByUri(uri);
		if ( oldSurveyWork == null && oldPublishedSurvey == null ) {
			throw new IllegalArgumentException("Survey to update not found: " + uri);
		} else if ( oldSurveyWork != null ) {
			updateSurveyWork(surveyFile, parsedSurvey, oldSurveyWork);
		} else {
			updatePublishedSurvey(surveyFile, parsedSurvey, false);
		}
		*/
		return parsedSurvey;
	}
	
	@Transactional
	public CollectSurvey updateModel(File surveyFile, CollectSurvey packagedSurvey)
			throws SurveyValidationException, SurveyImportException {
		String uri = packagedSurvey.getUri();
		CollectSurvey oldPublishedSurvey = getByUri(uri);
		if ( oldPublishedSurvey == null ) {
			throw new IllegalArgumentException("Survey to update not found: " + uri);
		}
		Integer id = oldPublishedSurvey.getId();
		packagedSurvey.setId(id);
		packagedSurvey.setName(oldPublishedSurvey.getName());
		
//		---- WARNING --- cannot check survey compatibility: code lists in packaged survey are empty
//		if ( validate ) {
//			surveyValidator.checkCompatibility(oldPublishedSurvey1, packagedSurvey);
//		}
		codeListManager.deleteAllItemsBySurvey(id, false);
		
		removeFromCache(oldPublishedSurvey);
		surveyDao.updateModel(packagedSurvey);
		addToCache(packagedSurvey);
		
		try {
			codeListManager.importCodeLists(packagedSurvey, surveyFile);
		} catch (CodeListImportException e) {
			throw new SurveyImportException(e);
		}
		return packagedSurvey;
	}
	
	@Transactional
	public CollectSurvey updateWorkModel(File surveyFile, boolean validate)
			throws SurveyValidationException, SurveyImportException {
		CollectSurvey parsedSurvey;
		try {
			parsedSurvey = unmarshalSurvey(surveyFile, validate, false);
		} catch (IdmlParseException e) {
			throw new SurveyImportException(e);
		}
		String uri = parsedSurvey.getUri();
		SurveySummary oldSurveyWork = loadWorkSummaryByUri(uri);
		if ( oldSurveyWork == null ) {
			throw new IllegalArgumentException("Survey to update not found: " + uri);
		} else {
			int oldSurveyId = oldSurveyWork.getId();
			parsedSurvey.setId(oldSurveyId);
			parsedSurvey.setName(oldSurveyWork.getName());
			parsedSurvey.setWork(true);
			
			//clean code list items
			for (CodeList codeList : parsedSurvey.getCodeLists()) {
				codeList.removeAllItems();
			}
			codeListManager.deleteAllItemsBySurvey(oldSurveyId, true);

			saveSurveyWork(parsedSurvey);
			
			//import code list items
			try {
				codeListManager.importCodeLists(parsedSurvey, surveyFile);
			} catch (CodeListImportException e) {
				throw new SurveyImportException(e);
			}
		}
		return parsedSurvey;
	}
	
	/**
	 * Import a survey and consider it as published.
	 * 
	 * @param survey
	 * @throws SurveyImportException
	 * @deprecated use {@link #importModel(File, String, boolean)} instead.
	 */
	@Transactional
	@Deprecated
	public void importModel(CollectSurvey survey) throws SurveyImportException {
		surveyDao.importModel(survey);
		addToCache(survey);
	}
	
	/**
	 * Updates a published survey and overwrites it with the specified one.
	 * The existing published survey will be searched by his URI.
	 * 
	 * @param survey
	 * @throws SurveyImportException
	 * @deprecated Use {@link #updateModel(File, boolean)} instead.
	 */
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
			if ( summary.isPublished() ) {
				int publishedSurveyId = summary.isWork() ? summary.getPublishedId(): summary.getId();
				summary.setRecordValidationProcessStatus(getRecordValidationProcessStatus(publishedSurveyId));
			}
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

	public SurveySummary getPublishedSummaryByName(String name) {
		CollectSurvey survey = get(name);
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
			surveySerializer.marshal(survey, os, marshalCodeLists,
					marshalPersistedCodeLists, marshalExternalCodeLists);
		} catch (IOException e) {
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

	/**
	 * Loads published and temporary survey summaries into a single list.
	 * Survey details like project name will be read using survey default language.
	 * 
	 * @return
	 */
	@Transactional
	public List<SurveySummary> loadSummaries() {
		return loadSummaries(null, false);
	}
	
	/**
	 * Loads published and temporary survey summaries into a single list.
	 * 
	 * @param labelLang 	language code used to 
	 * @param includeDetails if true, survey info like project name will be included in the summary (it makes the loading process slower).
	 * @return list of published and temporary surveys.
	 */
	@Transactional
	public List<SurveySummary> loadSummaries(String labelLang, boolean includeDetails) {
		List<SurveySummary> surveySummaries = getSurveySummaries(labelLang);
		List<SurveySummary> surveyWorkSummaries = loadWorkSummaries(labelLang, includeDetails);
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
				summaryWork.setRecordValidationProcessStatus(summary.getRecordValidationProcessStatus());
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
	public SurveySummary loadSummaryByName(String name) {
		SurveySummary workSummary = loadWorkSummaryByName(name);
		SurveySummary publishedSummary = getPublishedSummaryByName(name);
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
		CollectSurvey survey = surveyWorkDao.load(id);
		if ( survey != null ) {
			codeListManager.deleteInvalidCodeListReferenceItems(survey);
			survey.getUIOptions().removeUnassignedTabs();
			
			if ( survey.getSamplingDesignCodeList() == null ) {
				survey.addSamplingDesignCodeList();
			}
		}
		return survey;
	}
	
	@Transactional
	protected List<SurveySummary> loadWorkSummaries(String labelLang, boolean includeDetails) {
		List<SurveySummary> result = surveyWorkDao.loadSummaries();
		if ( includeDetails ) {
			for (SurveySummary summary : result) {
				CollectSurvey survey = surveyWorkDao.load(summary.getId());
				String projectName = survey.getProjectName(labelLang);
				if ( projectName == null && labelLang != null && ! labelLang.equals(survey.getDefaultLanguage()) ) {
					projectName = survey.getProjectName();
				}
				summary.setProjectName(projectName);
			}
		}
		return result;
	}
	
	@Transactional
	public SurveySummary loadWorkSummaryByUri(String uri) {
		return surveyWorkDao.loadSurveySummaryByUri(uri);
	}
	@Transactional
	public SurveySummary loadWorkSummaryByName(String name) {
		return surveyWorkDao.loadSurveySummaryByName(name);
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
	
	public CollectSurvey createSurveyWork(String name, String language) {
		CollectSurvey survey = (CollectSurvey) collectSurveyContext.createSurvey();
		survey.setName(name);
		survey.setUri(generateSurveyUri(name));
		survey.addLanguage(language);
		survey.setWork(true);
		return survey;
	}
	
	public String generateSurveyUri(String name) {
		return URI_PREFIX + name;
	}
	
	public String generateRandomSurveyUri() {
		return generateSurveyUri(UUID.randomUUID().toString());
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
		
		if ( surveyWork.getSamplingDesignCodeList() == null ) {
			surveyWork.addSamplingDesignCodeList();
		}
		return surveyWork;
	}
	
	@Transactional
	public void publish(CollectSurvey survey) throws SurveyImportException {
		codeListManager.deleteInvalidCodeListReferenceItems(survey);
		
		Integer surveyWorkId = survey.getId();
		survey.setWork(false);
		survey.setPublished(true);
		CollectSurvey oldPublishedSurvey = getByUri(survey.getUri());
		if ( oldPublishedSurvey == null ) {
			surveyDao.importModel(survey);
		} else {
			cancelRecordValidation(survey.getId());
			surveyDao.updateModel(survey);
		}
		int publishedSurveyId = survey.getId();
		samplingDesignManager.publishSamplingDesign(surveyWorkId, publishedSurveyId);
		speciesManager.publishTaxonomies(surveyWorkId, publishedSurveyId);
		codeListManager.publishCodeLists(surveyWorkId, publishedSurveyId);
		surveyWorkDao.delete(surveyWorkId);
		
		if ( oldPublishedSurvey != null ) {
			removeFromCache(oldPublishedSurvey);
		}
		addToCache(survey);
	}
	
	public void cancelRecordValidation(int surveyId) {
		ProcessStatus status = getRecordValidationProcessStatus(surveyId);
		if ( status != null ) {
			status.cancel();
		}
	}

	public void validateRecords(int surveyId, User user) {
		CollectSurvey survey = surveysById.get(surveyId);
		if ( survey == null ) {
			throw new IllegalStateException("Published survey not found, id="+surveyId);
		}
		ProcessStatus status = getRecordValidationProcessStatus(surveyId);
		if ( status != null && status.isRunning() ) {
			throw new IllegalStateException("Record validation process already started");
		} else {
			RecordValidationProcess process = applicationContext.getBean(RecordValidationProcess.class);
			process.setSurvey(survey);
			process.setUser(user);
			UUID sessionId = UUID.randomUUID();
			process.setSessionId(sessionId.toString());
			try {
				process.init();
				recordValidationStatusBySurvey.put(survey.getId(), process.getStatus());
				ExecutorServiceUtil.executeInCachedPool(process);
			} catch (Exception e) {
				LOG.error("Error validating survey records", e);
			}
		}
	}
	
	@Transactional
	public void deleteSurvey(int id) {
		if ( isRecordValidationInProgress(id) ) {
			cancelRecordValidation(id);
		}
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
	
	@Transactional
	public void removeVersion(CollectSurvey survey, ModelVersion version) {
		survey.removeVersion(version);
		codeListManager.removeVersioningReference(survey, version);
	}

	protected ProcessStatus getRecordValidationProcessStatus(int surveyId) {
		ProcessStatus status = recordValidationStatusBySurvey.get(surveyId);
		return status;
	}
	
	public boolean isRecordValidationInProgress(int surveyId) {
		ProcessStatus status = getRecordValidationProcessStatus(surveyId);
		return status != null && status.isRunning();
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
