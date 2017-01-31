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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.Collect;
import org.openforis.collect.datacleansing.manager.SurveyDataCleansingManager;
import org.openforis.collect.event.EventQueue;
import org.openforis.collect.event.SurveyUpdatedEvent;
import org.openforis.collect.io.exception.CodeListImportException;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.collect.manager.validation.RecordValidationProcess;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.metamodel.SurveySummarySortField;
import org.openforis.collect.metamodel.SurveySummarySortField.Sortable;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.Institution;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyFileDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.utils.ExecutorServiceUtil;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.metamodel.xml.internal.marshal.SurveyMarshaller.SurveyMarshalParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class SurveyManager {
	
	private static final Log LOG = LogFactory.getLog(SurveyManager.class);
	private static final List<SurveySummarySortField> DEFAULT_SURVEY_SUMMARY_SORT_FIELDS = 
			Arrays.asList(new SurveySummarySortField(Sortable.NAME));
	private static final String URI_PREFIX = "http://www.openforis.org/idm/";
	
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private SurveyFileDao surveyFileDao;
	@Autowired
	private SurveyDao surveyDao;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private CollectSurveyContext collectSurveyContext;
	@Autowired
	private SurveyValidator surveyValidator;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private CollectSurveyIdmlBinder surveySerializer;
	@Autowired(required=false)
	private EventQueue eventQueue;
	@Autowired(required=false)
	private SurveyDataCleansingManager dataCleansingManager;
	@Autowired(required=false)
	private InstitutionManager institutionManager;
	
	private Map<Integer, ProcessStatus> recordValidationStatusBySurvey;
	
	private SurveyCache publishedSurveyCache = null;
	
	public SurveyManager() {
		recordValidationStatusBySurvey = Collections.synchronizedMap(new HashMap<Integer, ProcessStatus>());
	}

	public void init() {
	}

	public List<CollectSurvey> getAll() {
		return CollectionUtils.unmodifiableList(getPublishedSurveyCache().surveys);
	}

	public CollectSurvey get(String name) {
		return getPublishedSurveyCache().getByName(name);
	}
	
	public CollectSurvey getById(int id) {
		return getPublishedSurveyCache().getById(id);
	}
	
	public CollectSurvey getByUri(String uri) {
		return getPublishedSurveyCache().getByUri(uri);
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey importTemporaryModel(InputStream is, String name, boolean validate)
			throws SurveyImportException, SurveyValidationException {
		File tempFile = null;
		try {
			tempFile = OpenForisIOUtils.copyToTempFile(is);
			return importTemporaryModel(tempFile, name, validate);
		} finally {
			if ( tempFile != null && tempFile.exists() ) {
				tempFile.delete();
			}
		}
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey importTemporaryModel(File surveyFile, String name, boolean validate) throws SurveyImportException, SurveyValidationException {
		try {
			CollectSurvey survey = unmarshalSurvey(surveyFile, validate, false);
			survey.setName(name);
			survey.setTemporary(true);
			surveyDao.insert(survey);
			codeListManager.importCodeLists(survey, surveyFile);
			return survey;
		} catch ( CodeListImportException e ) {
			throw new SurveyImportException(e);
		} catch (IdmlParseException e) {
			throw new SurveyImportException(e);
		}
	}

	/**
	 * Duplicates a published survey into a temporary survey and import the survey file into this new temporary survey
	 */
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey importInPublishedTemporaryModel(String uri, File surveyFile, boolean validate) 
			throws SurveyStoreException, SurveyValidationException {
		createTemporarySurveyFromPublished(uri);
		CollectSurvey newTemporarySurvey = updateTemporaryModel(surveyFile, validate);
		return newTemporarySurvey;
	}
	
	/**
	 * Imports a survey from a XML file input stream and publishes it.
	 */
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
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

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey importModel(File surveyFile, String name, boolean validate) throws SurveyImportException, SurveyValidationException {
		return importModel(surveyFile, name, validate, false);
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey importModel(File surveyFile, String name, boolean validate, boolean includeCodeLists) throws SurveyImportException, SurveyValidationException {
		try {
			CollectSurvey survey = unmarshalSurvey(surveyFile, validate, includeCodeLists);
			survey.setName(name);
			survey.setPublished(true);
			surveyDao.insert(survey);
			
			getPublishedSurveyCache().add(survey);
			
			codeListManager.importCodeLists(survey, surveyFile);
			return survey;
		} catch ( CodeListImportException e ) {
			throw new SurveyImportException(e);
		} catch (IdmlParseException e) {
			throw new SurveyImportException(e);
		}
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey updateModel(InputStream is, boolean validate) throws IdmlParseException, SurveyValidationException, SurveyImportException {
		File tempFile = OpenForisIOUtils.copyToTempFile(is);
		try {
			return updateModel(tempFile, validate);
		} finally {
			tempFile.delete();
		}
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey updateModel(File surveyFile, boolean validate)
			throws SurveyValidationException, SurveyImportException {
		return updateModel(surveyFile, validate, false);
	}
	
	/**
	 * Updates a published or a temporary survey and overwrites it with the specified one.
	 * The existing survey will be searched by his URI.
	 * If a temporary survey with the same URI as the survey in the surveyFile exists,
	 * than it will be overwritten with the passed one, otherwise the published survey will be overwritten.
	 */
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey updateModel(File surveyFile, boolean validate, boolean includeCodeLists)
			throws SurveyValidationException, SurveyImportException {
		CollectSurvey parsedSurvey;
		try {
			parsedSurvey = unmarshalSurvey(surveyFile, validate, includeCodeLists);
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
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
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
		
		getPublishedSurveyCache().remove(oldPublishedSurvey);
		
		surveyDao.update(packagedSurvey);

		getPublishedSurveyCache().add(packagedSurvey);
		
		try {
			codeListManager.importCodeLists(packagedSurvey, surveyFile);
		} catch (CodeListImportException e) {
			throw new SurveyImportException(e);
		}
		return packagedSurvey;
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey updateTemporaryModel(File surveyFile, boolean validate)
			throws SurveyValidationException, SurveyStoreException {
		CollectSurvey parsedSurvey;
		try {
			parsedSurvey = unmarshalSurvey(surveyFile, validate, false);
		} catch (IdmlParseException e) {
			throw new SurveyImportException(e);
		}
		String uri = parsedSurvey.getUri();
		SurveySummary oldTemporarySurvey = loadTemporarySummaryByUri(uri);
		if ( oldTemporarySurvey == null ) {
			throw new IllegalArgumentException("Survey to update not found: " + uri);
		} else {
			int oldSurveyId = oldTemporarySurvey.getId();
			parsedSurvey.setId(oldSurveyId);
			parsedSurvey.setName(oldTemporarySurvey.getName());
			parsedSurvey.setPublishedId(oldTemporarySurvey.getPublishedId());
			parsedSurvey.setTemporary(true);
			
			//clean code list items
			for (CodeList codeList : parsedSurvey.getCodeLists()) {
				codeList.removeAllItems();
			}
			codeListManager.deleteAllItemsBySurvey(oldSurveyId, true);

			save(parsedSurvey);
			
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
	@Transactional(readOnly=false, propagation=Propagation.SUPPORTS)
	@Deprecated
	public void importModel(CollectSurvey survey) throws SurveyImportException {
		surveyDao.insert(survey);
		getPublishedSurveyCache().add(survey);
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
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void updateModel(CollectSurvey survey) throws SurveyImportException {
		//remove old survey from surveys cache
		CollectSurvey oldSurvey = get(survey.getName());
		if ( oldSurvey != null ) {
			getPublishedSurveyCache().remove(oldSurvey);
		} else {
			throw new SurveyImportException("Could not find survey to update");
		}
		surveyDao.update(survey);
		getPublishedSurveyCache().add(survey);
	}

	public List<SurveySummary> getSurveySummaries(String lang) {
		return getSurveySummaries(lang, null);
	}
	
	public List<SurveySummary> getSurveySummaries(User availableToUser) {
		return getSurveySummaries(null, availableToUser);
	}
	
	public List<SurveySummary> getSurveySummaries(String lang, User availableToUser) {
		List<SurveySummary> summaries = new ArrayList<SurveySummary>();
		
		List<Long> userInstitutionIds;
		if (institutionManager == null || availableToUser == null) {
			userInstitutionIds = Collections.emptyList();
		} else {
			List<Institution> userInstitutions = institutionManager.findByUser(availableToUser);
			userInstitutionIds = CollectionUtils.project(userInstitutions, "id");
		}
		for (CollectSurvey survey : getPublishedSurveyCache().surveys) {
			if (availableToUser == null || userInstitutionIds.contains(survey.getInstitutionId())) {
				SurveySummary summary = SurveySummary.createFromSurvey(survey, lang);
				if ( summary.isPublished() ) {
					int publishedSurveyId = summary.isTemporary() ? summary.getPublishedId(): summary.getId();
					summary.setRecordValidationProcessStatus(getRecordValidationProcessStatus(publishedSurveyId));
				}
				summaries.add(summary);
			}
		}
		sortSummaries(summaries);
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
	
	protected void sortSummaries(List<SurveySummary> summaries) {
		sortSummaries(summaries, null);
	}
	
	protected void sortSummaries(List<SurveySummary> summaries, List<SurveySummarySortField> sortFields) {
		final List<SurveySummarySortField> sortFields2 = sortFields == null ? DEFAULT_SURVEY_SUMMARY_SORT_FIELDS: sortFields;
		Collections.sort(summaries, new Comparator<SurveySummary>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(SurveySummary s1, SurveySummary s2) {
				int result = 0;
				for (SurveySummarySortField sortField : sortFields2) {
					try {
						Object f1 = getFieldValue(s1, sortField);
						Object f2 = getFieldValue(s2, sortField);
						Object c1, c2; 
						if (sortField.isDescending()) {
							c1 = f2;
							c2 = f1;
						} else {
							c1 = f1;
							c2 = f2;
						}
						if (c1 == null && c2 == null) {
							return 0;
						} else if (c1 == null) {
							return -1;
						} else if (c2 == null) {
							return 1;
						} else if (c1 instanceof Date) {
							result = DateUtils.truncatedCompareTo((Date) c1, (Date) c2, Calendar.SECOND);
						} else if (c1 instanceof Comparable) {
							result = ((Comparable) c1).compareTo((Comparable) c2);
						} else if (c1 instanceof Enum) {
							result = ((Enum) c1).name().compareTo(((Enum) c2).name());
						} else {
							throw new IllegalArgumentException("Survey summary sort error - unsupported type: " + c1.getClass().getName());
						}
						if (result != 0) {
							return result;
						}
					} catch (Exception e) {
						throw new RuntimeException(e); //should never happen
					}
				}
				return result;
			}

			private Object getFieldValue(SurveySummary s, SurveySummarySortField sortField) {
				switch(sortField.getField()) {
				case MODIFIED:
					return s.isTemporary();
				case MODIFIED_DATE:
					return s.getModifiedDate();
				case NAME:
					return s.getName();
				case PROJECT_NAME:
					return s.getProjectName();
				case PUBLISHED:
					return s.isPublished();
				case TARGET:
					return s.getTarget();
				default:
					return null;
				}
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
		marshalSurvey(survey, os, new SurveyMarshalParameters(marshalCodeLists, marshalPersistedCodeLists, 
				marshalExternalCodeLists, survey.getDefaultLanguage()));
	}
	
	public void marshalSurvey(Survey survey, OutputStream os, SurveyMarshalParameters parameters) {
		try {
			surveySerializer.marshal(survey, os, parameters);
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
	public List<SurveySummary> loadCombinedSummaries() {
		return loadCombinedSummaries(null, false);
	}
	
	/**
	 * Loads published and temporary survey summaries into a single list.
	 * 
	 * @param labelLang 	language code used to 
	 * @param includeDetails if true, survey info like project name will be included in the summary (it makes the loading process slower).
	 * @return list of published and temporary surveys.
	 */
	public List<SurveySummary> loadCombinedSummaries(String labelLang, boolean includeDetails) {
		return loadCombinedSummaries(labelLang, includeDetails, null);
	}
	
	/**
	 * Loads published and temporary survey summaries into a single list.
	 * 
	 * @param labelLang 	language code used to 
	 * @param includeDetails if true, survey info like project name will be included in the summary (it makes the loading process slower).
	 * @return list of published and temporary surveys.
	 */
	public List<SurveySummary> loadCombinedSummaries(String labelLang, boolean includeDetails, List<SurveySummarySortField> sortFields) {
		List<SurveySummary> publishedSurveySummaries = getSurveySummaries(labelLang);
		List<SurveySummary> temporarySurveySummaries = loadTemporarySummaries(labelLang, includeDetails);
		List<SurveySummary> result = new ArrayList<SurveySummary>();
		Map<String, SurveySummary> summariesByUri = new HashMap<String, SurveySummary>();
		for (SurveySummary summary : temporarySurveySummaries) {
			summary.setPublished(false);
			summary.setTemporary(true);
			result.add(summary);
			summariesByUri.put(summary.getUri(), summary);
		}
		for (SurveySummary publishedSurveySummary : publishedSurveySummaries) {
			SurveySummary temporarySurveySummary = summariesByUri.get(publishedSurveySummary.getUri());
			if ( temporarySurveySummary == null ) {
				result.add(publishedSurveySummary);
			} else {
				temporarySurveySummary.setPublished(true);
				temporarySurveySummary.setRecordValidationProcessStatus(publishedSurveySummary.getRecordValidationProcessStatus());
			}
		}
		sortSummaries(result, sortFields);
		return result;
	}
	
	public SurveySummary loadSummaryByUri(String uri) {
		SurveySummary temporarySummary = loadTemporarySummaryByUri(uri);
		SurveySummary publishedSummary = getPublishedSummaryByUri(uri);
		SurveySummary result = combineSummaries(temporarySummary,
				publishedSummary);
		return result;
	}

	public SurveySummary loadSummaryByName(String name) {
		SurveySummary temporarySummary = loadTemporarySummaryByName(name);
		SurveySummary publishedSummary = getPublishedSummaryByName(name);
		SurveySummary result = combineSummaries(temporarySummary,
				publishedSummary);
		return result;
	}
	
	private SurveySummary combineSummaries(SurveySummary temporarySummary,
			SurveySummary publishedSummary) {
		SurveySummary result; 
		if ( temporarySummary != null ) {
			result = temporarySummary;
			if ( publishedSummary != null ) {
				result.setPublished(true);
				result.setPublishedId(publishedSummary.getId());
			}
		} else {
			result = publishedSummary;
		}
		return result;
	}
	
	public CollectSurvey loadSurvey(int id) {
		CollectSurvey survey = surveyDao.loadById(id);
		if ( survey != null ) {
			codeListManager.deleteInvalidCodeListReferenceItems(survey);
			survey.getUIOptions().removeUnassignedTabs();
			
			if ( survey.getSamplingDesignCodeList() == null ) {
				survey.addSamplingDesignCodeList();
			}
			fillReferencedItems(survey);
		}
		return survey;
	}

	public CollectSurvey getOrLoadSurveyById(int id) {
		CollectSurvey survey = getById(id);
		if (survey == null) {
			survey = loadSurvey(id);
		}
		return survey;
	}
	
	public List<SurveySummary> loadTemporarySummaries(String labelLang, boolean includeDetails) {
		List<SurveySummary> summaries = surveyDao.loadTemporarySummaries();
		if ( includeDetails ) {
			for (SurveySummary summary : summaries) {
				CollectSurvey survey = surveyDao.loadById(summary.getId());
				String projectName = survey.getProjectName(labelLang);
				if ( projectName == null && labelLang != null && ! labelLang.equals(survey.getDefaultLanguage()) ) {
					projectName = survey.getProjectName();
				}
				summary.setProjectName(projectName);
				summary.setDefaultLanguage(survey.getDefaultLanguage());
				summary.setLanguages(survey.getLanguages());
			}
		}
		return summaries;
	}
	
	public SurveySummary loadTemporarySummaryByUri(String uri) {
		return surveyDao.loadSurveySummaryByUri(uri, true);
	}

	public SurveySummary loadTemporarySummaryByName(String name) {
		return surveyDao.loadSurveySummaryByName(name, true);
	}	
	
	public boolean isSurveyTemporary(CollectSurvey survey) {
		Integer id = survey.getId();
		String uri = survey.getUri();
		SurveySummary temporarySurveySummary = loadTemporarySummaryByUri(uri);
		if (temporarySurveySummary == null || ! temporarySurveySummary.getId().equals(id) ) {
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
	
	public CollectSurvey createTemporarySurvey(String name, String language) {
		CollectSurvey survey = (CollectSurvey) collectSurveyContext.createSurvey();
		survey.setName(name);
		survey.setUri(generateSurveyUri(name));
		survey.addLanguage(language);
		survey.setTemporary(true);
		if (survey.getSamplingDesignCodeList() == null) {
			survey.addSamplingDesignCodeList();
		}
		return survey;
	}
	
	public String generateSurveyUri(String name) {
		return URI_PREFIX + name;
	}
	
	public String generateRandomSurveyUri() {
		return generateSurveyUri(UUID.randomUUID().toString());
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void save(CollectSurvey survey) throws SurveyStoreException {
		survey.setModifiedDate(new Date());
		survey.setCollectVersion(Collect.VERSION);
		Integer id = survey.getId();
		if ( id == null ) {
			surveyDao.insert(survey);
		} else {
			surveyDao.update(survey);
		}
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey createTemporarySurveyFromPublished(String uri) {
		return createTemporarySurveyFromPublished(uri, true, true);
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey createTemporarySurveyFromPublished(String uri, boolean markCopyAsPublished, boolean preserveReferenceToPublishedSurvey) {
		try {
			SurveySummary existingTemporarySurvey = surveyDao.loadSurveySummaryByUri(uri, true);
			if ( existingTemporarySurvey != null ) {
				throw new IllegalArgumentException("Temporary survey with uri " + uri + " already existing");
			}
			CollectSurvey publishedSurvey = surveyDao.loadByUri(uri, false);
			int publishedSurveyId = publishedSurvey.getId();

			CollectSurvey temporarySurvey = publishedSurvey; //the published survey object should be cloned...
			temporarySurvey.setId(null);
			temporarySurvey.setPublished(markCopyAsPublished);
			temporarySurvey.setTemporary(true);
			temporarySurvey.setPublishedId(preserveReferenceToPublishedSurvey ? publishedSurveyId : null);
			if ( temporarySurvey.getSamplingDesignCodeList() == null ) {
				temporarySurvey.addSamplingDesignCodeList();
			}
			surveyDao.insert(temporarySurvey);
			
			publishedSurvey = getByUri(uri);
			
			copyReferencedMetadata(publishedSurvey, temporarySurvey);
			
			return temporarySurvey;
		} catch (SurveyImportException e) {
			//it should never enter here, we are duplicating an already existing survey
			throw new RuntimeException(e);
		}
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey duplicateSurveyIntoTemporary(String originalSurveyName, boolean originalSurveyIsTemporary, String newName) {
		try {
			CollectSurvey oldSurvey = loadSurvey(originalSurveyName, originalSurveyIsTemporary);

			//TODO : clone it
			CollectSurvey newSurvey = oldSurvey;
			newSurvey.setId(null);
			newSurvey.setPublished(false);
			newSurvey.setTemporary(true);
			newSurvey.setPublishedId(null);
			newSurvey.setName(newName);
			newSurvey.setUri(generateSurveyUri(newName));
			newSurvey.setCreationDate(new Date());
			newSurvey.setModifiedDate(new Date());

			if ( newSurvey.getSamplingDesignCodeList() == null ) {
				newSurvey.addSamplingDesignCodeList();
			}
			surveyDao.insert(newSurvey);
			
			//reload old survey, it has been modified previously
			oldSurvey = loadSurvey(originalSurveyName, originalSurveyIsTemporary);
			
			copyReferencedMetadata(oldSurvey, newSurvey);
			
			return newSurvey;
		} catch (SurveyImportException e) {
			//it should never enter here, we are duplicating an already existing survey
			throw new RuntimeException(e);
		}
	}

	private void copyReferencedMetadata(CollectSurvey fromSurvey,
			CollectSurvey toSurvey) {
		int toSurveyId = toSurvey.getId();
		int fromSurveyId = fromSurvey.getId();
		samplingDesignManager.copySamplingDesign(fromSurveyId, toSurveyId);
		speciesManager.copyTaxonomy(fromSurveyId, toSurveyId);
		codeListManager.copyCodeLists(fromSurvey, toSurvey);
		surveyFileDao.copyItems(fromSurvey.getId(), toSurvey.getId());
		
		if (dataCleansingManager != null) {
			dataCleansingManager.duplicateMetadata(fromSurvey, toSurvey);
		}
	}
	
	private CollectSurvey loadSurvey(String name, boolean temporary) {
		return surveyDao.loadByName(name, temporary);
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void publish(CollectSurvey survey) throws SurveyImportException {
		codeListManager.deleteInvalidCodeListReferenceItems(survey);
		
		Integer temporarySurveyId = survey.getId();
		
		Integer oldPublishedSurveyId = survey.getPublishedId();
		boolean existsPublishedSurvey = oldPublishedSurveyId != null;
		if (existsPublishedSurvey) {
			cancelRecordValidation(oldPublishedSurveyId);
			survey.setId(oldPublishedSurveyId);
		}
		survey.setTemporary(false);
		survey.setPublished(true);
		survey.setPublishedId(null);
		survey.setModifiedDate(new Date());
		survey.setCollectVersion(Collect.VERSION);

		surveyDao.update(survey);
		int newSurveyId = survey.getId();
		
		if (newSurveyId != temporarySurveyId) {
			if (dataCleansingManager != null) {
				//do not overwrite published cleansing metadata
				CollectSurvey temporarySurvey = surveyDao.loadById(temporarySurveyId);
				dataCleansingManager.moveMetadata(temporarySurvey, survey);
			}
			samplingDesignManager.moveSamplingDesign(temporarySurveyId, newSurveyId);
			speciesManager.moveTaxonomies(temporarySurveyId, newSurveyId);
			codeListManager.moveCodeLists(temporarySurveyId, newSurveyId);
			surveyFileDao.deleteBySurvey(newSurveyId);
			surveyFileDao.moveItems(temporarySurveyId, newSurveyId);
			
			surveyDao.delete(temporarySurveyId);
			CollectSurvey oldPublishedSurvey = getById(oldPublishedSurveyId);
			if (oldPublishedSurvey != null) {
				getPublishedSurveyCache().remove(oldPublishedSurvey);
			}
		}
		getPublishedSurveyCache().add(survey);
		
		if (eventQueue != null && eventQueue.isEnabled()) {
			eventQueue.publish(new SurveyUpdatedEvent(survey.getName()));
		}
	}
	
	/**
	 * If no temporary survey is associated to the published one with the specified id, 
	 * it duplicates the published survey into a temporary one, then removes the published one.
	 */
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public CollectSurvey unpublish(int surveyId) throws SurveyStoreException {
		CollectSurvey publishedSurvey = getById(surveyId);
		String uri = publishedSurvey.getUri();
		
		SurveySummary temporarySurveySummary = surveyDao.loadSurveySummaryByUri(uri, true);
		CollectSurvey temporarySurvey;
		if (temporarySurveySummary == null) {
			temporarySurvey = createTemporarySurveyFromPublished(uri, false, false);
		} else {
			temporarySurvey = loadSurvey(temporarySurveySummary.getId());
			temporarySurvey.setPublished(false);
			temporarySurvey.setPublishedId(null);
			save(temporarySurvey);
			if (dataCleansingManager != null) {
				//overwrite temporary cleansing metadata with published one
				dataCleansingManager.moveMetadata(publishedSurvey, temporarySurvey);
			}
		}
		//delete published survey
		deleteSurvey(surveyId);
		
		return temporarySurvey;
	}
	
	public void cancelRecordValidation(int surveyId) {
		ProcessStatus status = getRecordValidationProcessStatus(surveyId);
		if ( status != null ) {
			status.cancel();
		}
	}

	public void validateRecords(int surveyId, User user) {
		CollectSurvey survey = getPublishedSurveyCache().getById(surveyId);
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
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void deleteSurvey(int id) {
		if ( isRecordValidationInProgress(id) ) {
			cancelRecordValidation(id);
		}
		CollectSurvey publishedSurvey = getById(id);
		boolean temporary = publishedSurvey == null;

		//delete records
		if ( ! temporary ) {
			recordDao.deleteBySurvey(id);
		}

		//delete metadata
		speciesManager.deleteTaxonomiesBySurvey(id);
		samplingDesignManager.deleteBySurvey(id);
		codeListManager.deleteAllItemsBySurvey(id, temporary);
		surveyFileDao.deleteBySurvey(id);
		
		if (dataCleansingManager != null) {
			CollectSurvey survey = loadSurvey(id);
			dataCleansingManager.deleteMetadata(survey);
		}
		
		//delete survey
		surveyDao.delete(id);
		
		if ( ! temporary ) {
			getPublishedSurveyCache().remove(publishedSurvey);
		}
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void removeVersion(CollectSurvey survey, ModelVersion version) {
		survey.removeVersion(version);
		codeListManager.removeVersioningReference(survey, version);
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void updateLanguages(CollectSurvey survey, List<String> newLanguageCodes) {
		codeListManager.updateSurveyLanguages(survey, newLanguageCodes);
		
		List<String> oldLangCodes = new ArrayList<String>(survey.getLanguages());
		// remove languages from survey
		for (String oldLangCode : oldLangCodes) {
			if (! newLanguageCodes.contains(oldLangCode)) {
				survey.removeLanguage(oldLangCode);
			}
		}
		// add new languages
		for (String lang : newLanguageCodes) {
			if ( ! oldLangCodes.contains(lang) ) {
				survey.addLanguage(lang);
			}
		}
		// sort languages
		for (int i = 0; i < newLanguageCodes.size(); i++) {
			String lang = newLanguageCodes.get(i);
			survey.moveLanguage(lang, i);
		}

	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void addSurveyFile(CollectSurvey survey, SurveyFile file, File content) {
		try {
			surveyFileDao.insert(file);
			byte[] contentBytes = FileUtils.readFileToByteArray(content);
			surveyFileDao.updateContent(file, contentBytes);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void updateSurveyFile(CollectSurvey survey, SurveyFile file, File content) {
		try {
			surveyFileDao.update(file);
			if (content != null) {
				byte[] contentBytes = FileUtils.readFileToByteArray(content);
				surveyFileDao.updateContent(file, contentBytes);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void fillReferencedItems(Collection<CollectSurvey> surveys) {
		for (CollectSurvey survey : surveys) {
			fillReferencedItems(survey);
		}
	}
	
	private void fillReferencedItems(CollectSurvey survey) {
		if (institutionManager != null) {
			Institution institution = null;
			if (survey.getInstitutionId() == null) {
				//use default public institution
				institution = institutionManager.findByName(InstitutionManager.DEFAULT_PUBLIC_INSTITUTION_NAME);
			} else {
				institution = institutionManager.findById(survey.getInstitutionId());
			}
			survey.setInstitution(institution);
		}
	}

	public byte[] loadSurveyFileContent(SurveyFile surveyFile) {
		return surveyFileDao.loadContent(surveyFile);
	}

	public List<SurveyFile> loadSurveyFileSummaries(CollectSurvey survey) {
		return surveyFileDao.loadBySurvey(survey);
	}
	
	public void deleteSurveyFile(SurveyFile surveyFile) {
		surveyFileDao.delete(surveyFile.getId());
	}
	
	public void deleteSurveyFiles(CollectSurvey survey) {
		surveyFileDao.deleteBySurvey(survey.getId());
	}
	
	protected ProcessStatus getRecordValidationProcessStatus(int surveyId) {
		ProcessStatus status = recordValidationStatusBySurvey.get(surveyId);
		return status;
	}
	
	public boolean isRecordValidationInProgress(int surveyId) {
		ProcessStatus status = getRecordValidationProcessStatus(surveyId);
		return status != null && status.isRunning();
	}

	private SurveyCache getPublishedSurveyCache() {
		if (publishedSurveyCache == null) {
			publishedSurveyCache = new SurveyCache();
		}
		return publishedSurveyCache;
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

	public CollectSurveyIdmlBinder getSurveySerializer() {
		return surveySerializer;
	}
	
	public void setSurveySerializer(CollectSurveyIdmlBinder surveySerializer) {
		this.surveySerializer = surveySerializer;
	}
	
	private class SurveyCache {
		
		private List<CollectSurvey> surveys = new ArrayList<CollectSurvey>();
		private Map<Integer, CollectSurvey> surveysById = new HashMap<Integer, CollectSurvey>();
		private Map<String, CollectSurvey> surveysByName = new HashMap<String, CollectSurvey>();
		private Map<String, CollectSurvey> surveysByUri = new HashMap<String, CollectSurvey>();
		
		public SurveyCache() {
			populate();
		}
		
		private void populate() {
			surveys = surveyDao.loadAllPublished();
			fillReferencedItems(surveys);
			for (CollectSurvey survey : surveys) {
				add(survey);
			}
		}
		
		public CollectSurvey getById(int id) {
			return surveysById.get(id);
		}
		
		public CollectSurvey getByName(String name) {
			return surveysByName.get(name);
		}
		
		public CollectSurvey getByUri(String uri) {
			return surveysByUri.get(uri);
		}
		
		private void add(CollectSurvey survey) {
			if ( ! surveys.contains(survey) ) {
				surveys.add(survey);
			}
			surveysById.put(survey.getId(), survey);
			surveysByName.put(survey.getName(), survey);
			surveysByUri.put(survey.getUri(), survey);
		}
		
		protected void remove(CollectSurvey survey) {
			surveys.remove(survey);
			surveysById.remove(survey.getId());
			surveysByName.remove(survey.getName());
			surveysByUri.remove(survey.getUri());
		}
		
	}

}
