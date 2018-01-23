package org.openforis.collect.web.controller;

import static org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreator.PLACEMARK_FILE_NAME;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.io.AbstractSurveyRestoreJob;
import org.openforis.collect.io.CESurveyRestoreJob;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.io.SurveyBackupInfoExtractorJob;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyRestoreJob;
import org.openforis.collect.io.XMLSurveyRestoreJob;
import org.openforis.collect.io.ZipFileExtractor;
import org.openforis.collect.manager.CollectEarthSurveyExportJob;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.SurveyObjectsGenerator;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.metamodel.view.SurveyView;
import org.openforis.collect.metamodel.view.SurveyViewGenerator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.Files;
import org.openforis.collect.web.controller.CollectJobController.JobView;
import org.openforis.collect.web.controller.SurveyController.SurveyCloneParameters.SurveyType;
import org.openforis.collect.web.controller.SurveyController.SurveyCreationParameters.TemplateType;
import org.openforis.collect.web.validator.SimpleSurveyCreationParametersValidator;
import org.openforis.collect.web.validator.SurveyCloneParametersValidator;
import org.openforis.collect.web.validator.SurveyCreationParametersValidator;
import org.openforis.collect.web.validator.SurveyImportParametersValidator;
import org.openforis.commons.web.Response;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.JobManager;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("/api/survey")
@Scope(SCOPE_SESSION)
public class SurveyController extends BasicController {

	private static final String IDM_TEMPLATE_FILE_NAME_FORMAT = "/org/openforis/collect/designer/templates/%s.idm.xml";
	public static final String DEFAULT_ROOT_ENTITY_NAME = "change_it_to_your_sampling_unit";
	public static final String DEFAULT_MAIN_TAB_LABEL = "Change it to your main tab label";
	private static final String COLLECT_EARTH_PROJECT_FILE_EXTENSION = "cep";

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserGroupManager userGroupManager;
	@Autowired
	private JobManager jobManager;
	@Autowired
	private SessionManager sessionManager;
//	@Autowired
//	private CollectEarthSurveyValidator collectEarthSurveyValidator;

	//validators
	@Autowired
	private SurveyCreationParametersValidator surveyCreationParametersValidator;
	@Autowired
	private SimpleSurveyCreationParametersValidator simpleSurveyCreationParametersValidator;
	@Autowired
	private SurveyImportParametersValidator surveyImportParametersValidator;
	@Autowired
	private SurveyCloneParametersValidator surveyCloneParametersValidator;

	private SurveyBackupInfoExtractorJob surveyBackupInfoExtractorJob;
	private File uploadedSurveyFile;
	private SurveyBackupInfo uploadedSurveyInfo;
	private AbstractSurveyRestoreJob surveyImportJob;
	private Job surveyBackupJob;
	private SurveyCloneJob surveyCloneJob;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		Object target = binder.getTarget();
		if (target != null) {
			if (target instanceof SurveyCreationParameters) {
				binder.setValidator(surveyCreationParametersValidator);
			} else if (target instanceof SimpleSurveyCreationParameters) {
				binder.setValidator(simpleSurveyCreationParametersValidator);
			} else if (target instanceof SurveyImportParameters) {
				binder.setValidator(surveyImportParametersValidator);
			} else if (target instanceof SurveyCloneParameters) {
				binder.setValidator(surveyCloneParametersValidator);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(method=GET)
	public @ResponseBody
	List<?> loadSurveys(
			@RequestParam(value="userId", required=false) Integer userId,
			@RequestParam(value="groupId", required=false) Integer groupId,
			@RequestParam(value="full", required=false) boolean fullSurveys,
			@RequestParam(value="includeCodeListValues", required=false) boolean includeCodeListValues,
			@RequestParam(value="includeTemporary", required=false) boolean includeTemporary) throws Exception {
		String languageCode = Locale.ENGLISH.getLanguage();
		if (userId == null) {
			userId = sessionManager.getLoggedUser().getId();
		}
		Set<UserGroup> groups = getAvailableUserGrups(userId, groupId);
		
		List<SurveySummary> summaries = new ArrayList<SurveySummary>(surveyManager.getSurveySummaries(languageCode, groups));
		if (includeTemporary) {
			summaries.addAll(surveyManager.loadTemporarySummaries(languageCode, true, groups));
		}
		
		List<Object> views = new ArrayList<Object>();
		for (SurveySummary surveySummary : summaries) {
			if (fullSurveys) {
				CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveySummary.getId());
				views.add(generateView(survey, includeCodeListValues));
			} else {
				views.add(surveySummary);
			}
		}
		views.sort(Collections.reverseOrder(new BeanComparator("modifiedDate")));
		return views;
	}

	@RequestMapping(value="{id}", method=GET)
	public @ResponseBody
	SurveyView loadSurvey(@PathVariable int id, 
			@RequestParam(value="includeCodeListValues", required=false, defaultValue="true") boolean includeCodeListValues) 
			throws Exception {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		return generateView(survey, includeCodeListValues);
	}
	
	@Transactional
	@RequestMapping(method=POST)
	public @ResponseBody
	Response createSurvey(@Valid SurveyCreationParameters params, BindingResult bindingResult) throws Exception {
		if (bindingResult.hasErrors()) {
			Response res = new Response();
			res.setErrorStatus();
			res.addObject("errors", bindingResult.getFieldErrors());
			return res;
		}
		CollectSurvey survey;
		switch (params.getTemplateType()) {
		case BLANK:
			survey = createEmptySurvey(params.getName(), params.getDefaultLanguageCode());
			break;
		default:
			survey = createNewSurveyFromTemplate(params.getName(), params.getDefaultLanguageCode(), params.getTemplateType());
		}
		UserGroup userGroup = userGroupManager.loadById(params.getUserGroupId());
		survey.setUserGroupId(userGroup.getId());
		surveyManager.save(survey);
		
		SurveySummary surveySummary = SurveySummary.createFromSurvey(survey);
		Response res = new Response();
		res.setObject(surveySummary);
		return res;
	}
	
	@Transactional
	@RequestMapping(value="cloneintotemporary/{surveyId}", method=POST)
	public @ResponseBody
	Response cloneIntoTemporarySurvey(@PathVariable int surveyId) throws Exception {
		Response response = new Response();
		User loggedUser = sessionManager.getLoggedUser();
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		if (survey.isPublished()) {
			String surveyUri = survey.getUri();
			CollectSurvey temporarySurvey = surveyManager.createTemporarySurveyFromPublished(surveyUri, loggedUser);
			response.setObject(temporarySurvey.getId());
		} else {
			response.setErrorStatus();
			response.setErrorMessage(String.format("Survey with id %d is not published as expected", surveyId));
		}
		return response;
	}
	
	
	@RequestMapping(value="validatecreation", method=POST)
	public @ResponseBody Response validateSurveyCreationParameters(@Valid SurveyCreationParameters params, BindingResult result) {
		return generateFormValidationResponse(result);
	}
	
	private CollectSurvey createNewSurveyFromTemplate(String name, String langCode, TemplateType templateType)
			throws IdmlParseException, SurveyValidationException {
		String templateFileName = String.format(IDM_TEMPLATE_FILE_NAME_FORMAT, templateType.name().toLowerCase(Locale.ENGLISH));
		InputStream surveyFileIs = this.getClass().getResourceAsStream(templateFileName);
		CollectSurvey survey = surveyManager.unmarshalSurvey(surveyFileIs, false, true);
		survey.setName(name);
		survey.setTemporary(true);
		survey.setUri(surveyManager.generateSurveyUri(name));
		survey.setDefaultLanguage(langCode);
		SurveyTarget target;
		switch (templateType) {
		case COLLECT_EARTH:
		case COLLECT_EARTH_IPCC:
			target = SurveyTarget.COLLECT_EARTH;
			break;
		default:
			target = SurveyTarget.COLLECT_DESKTOP;
		}
		survey.setTarget(target);
		
		if ( survey.getSamplingDesignCodeList() == null ) {
			survey.addSamplingDesignCodeList();
		}
		return survey;
	}

	private CollectSurvey createEmptySurvey(String name, String langCode) {
		//create empty survey
		CollectSurvey survey = surveyManager.createTemporarySurvey(name, langCode);
		//add default root entity
		Schema schema = survey.getSchema();
		EntityDefinition rootEntity = schema.createEntityDefinition();
		rootEntity.setMultiple(true);
		rootEntity.setName(DEFAULT_ROOT_ENTITY_NAME);
		schema.addRootEntityDefinition(rootEntity);
		//create root tab set
		UIOptions uiOptions = survey.getUIOptions();
		UITabSet rootTabSet = uiOptions.createRootTabSet((EntityDefinition) rootEntity);
		UITab mainTab = uiOptions.getMainTab(rootTabSet);
		mainTab.setLabel(langCode, DEFAULT_MAIN_TAB_LABEL);
		
		SurveyObjectsGenerator surveyObjectsGenerator = new SurveyObjectsGenerator();
		surveyObjectsGenerator.addPredefinedObjects(survey);
		
		return survey;
	}
	
	@RequestMapping(value="publish/{id}", method=POST)
	public @ResponseBody SurveyView publishSurvey(@PathVariable int id) throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		User activeUser = sessionManager.getLoggedUser();
		surveyManager.publish(survey, activeUser);
		return generateView(survey, false);
	}
	
	@RequestMapping(value="unpublish/{id}", method=POST)
	public @ResponseBody SurveyView unpublishSurvey(@PathVariable int id) throws SurveyStoreException {
		User activeUser = sessionManager.getLoggedUser();
		CollectSurvey survey = surveyManager.unpublish(id, activeUser);
		return generateView(survey, false);
	}
	
	@RequestMapping(value="close/{id}", method=POST)
	public @ResponseBody SurveyView closeSurvey(@PathVariable int id) throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		surveyManager.close(survey);
		return generateView(survey, false);
	}
	
	@RequestMapping(value="archive/{id}", method=POST)
	public @ResponseBody SurveyView archiveSurvey(@PathVariable int id) throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		surveyManager.archive(survey);
		return generateView(survey, false);
	}
	
	@RequestMapping(value="delete/{id}", method=POST)
	public @ResponseBody Response deleteSurvey(@PathVariable int id) throws SurveyImportException {
		surveyManager.deleteSurvey(id);
		return new Response();
	}
	
	@RequestMapping(value="clone", method=POST)
	public @ResponseBody JobView cloneSurvey(@Valid SurveyCloneParameters params) {
		surveyCloneJob = new SurveyCloneJob(surveyManager);
		surveyCloneJob.setOriginalSurveyName(params.originalSurveyName);
		surveyCloneJob.setNewName(params.newSurveyName);
		surveyCloneJob.setOriginalSurveyType(params.originalSurveyType);
		surveyCloneJob.setActiveUser(sessionManager.getLoggedUser());
		jobManager.start(surveyCloneJob);
		return new JobView(surveyCloneJob);
	}
	
	@RequestMapping(value="cloned/id", method=GET)
	public @ResponseBody Response getClonedSurveyId() {
		Response response = new Response();
		if (surveyCloneJob == null || !surveyCloneJob.isCompleted()) {
			response.setErrorStatus();
			response.setErrorMessage("Survey clone job not found");
		} else {
			response.setObject(surveyCloneJob.getOutputSurvey().getId());
		}
		return response;
	}
	
	@RequestMapping(value="validate/clone", method=POST)
	public @ResponseBody Response validateSurveyCloneParameters(@Valid SurveyCloneParameters params, BindingResult result) {
		return generateFormValidationResponse(result);
	}
	
	@RequestMapping(value = "prepareimport", method=POST, consumes=MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody
	Response prepareSurveyImport(@RequestParam("file") MultipartFile multipartFile) throws IOException {
		String fileName = multipartFile.getOriginalFilename();
		File tempFile = Files.writeToTempFile(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), "ofc_csv_data_import");
		String extension = FilenameUtils.getExtension(fileName);

		this.uploadedSurveyFile = tempFile;
		
		if ( surveyBackupInfoExtractorJob != null && surveyBackupInfoExtractorJob.isRunning() ) {
			surveyBackupInfoExtractorJob.abort();
		}
		surveyBackupInfoExtractorJob = jobManager.createJob(SurveyBackupInfoExtractorJob.class);
		
		if (COLLECT_EARTH_PROJECT_FILE_EXTENSION.equalsIgnoreCase(extension)) {
			File idmFile = extractIdmFromCEPFile(tempFile);
			surveyBackupInfoExtractorJob.setFile(idmFile);
		} else {
			surveyBackupInfoExtractorJob.setFile(tempFile);
		}
		surveyBackupInfoExtractorJob.setValidate(false);
		
		jobManager.start(surveyBackupInfoExtractorJob, false);
		
		Response response = new Response();
		if (surveyBackupInfoExtractorJob.isCompleted()) {
			uploadedSurveyInfo = surveyBackupInfoExtractorJob.getInfo();
			response.addObject("surveyBackupInfo", uploadedSurveyInfo);
			SurveySummary existingSummary = surveyManager.loadSummaryByUri(uploadedSurveyInfo.getSurveyUri());
			response.addObject("importingIntoExistingSurvey", existingSummary != null);
			response.addObject("existingSurveyUserGroupId", existingSummary == null ? null : existingSummary.getUserGroupId());
			return response;
		} else {
			response.setErrorStatus();
			response.setErrorMessage(surveyBackupInfoExtractorJob.getErrorMessage());
			return response;
		}
	}
	
	@RequestMapping(value="validateimport", method=POST)
	public @ResponseBody Response validateSurveyImportParameters(@Valid SurveyImportParameters params, BindingResult result) {
		return generateFormValidationResponse(result);
	}

	@RequestMapping(value = "startimport", method=POST)
	public @ResponseBody Response startSurveyFileImport(@Valid SurveyImportParameters params, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			Response res = new Response();
			res.setErrorStatus();
			res.addObject("errors", bindingResult.getFieldErrors());
			return res;
		}
		String surveyName = params.getName();
		UserGroup userGroup = userGroupManager.loadById(params.getUserGroupId());
		
		String uploadedFileNameExtension = FilenameUtils.getExtension(this.uploadedSurveyFile.getName());
		AbstractSurveyRestoreJob job;
		if ( Files.XML_FILE_EXTENSION.equalsIgnoreCase(uploadedFileNameExtension) ) {
			job = jobManager.createJob(XMLSurveyRestoreJob.class);
		} else if (COLLECT_EARTH_PROJECT_FILE_EXTENSION.equalsIgnoreCase(uploadedFileNameExtension)) {
			job= jobManager.createJob(CESurveyRestoreJob.class);
		} else {
			job= jobManager.createJob(SurveyRestoreJob.class);
		}
		job.setFile(this.uploadedSurveyFile);
		job.setSurveyName(surveyName);
		job.setSurveyUri(uploadedSurveyInfo == null ? null : uploadedSurveyInfo.getSurveyUri());
		job.setUserGroup(userGroup);
		job.setRestoreIntoPublishedSurvey(false);
		job.setValidateSurvey(false);
		job.setActiveUser(sessionManager.getLoggedUser());
		jobManager.start(job);
		this.surveyImportJob = job;
		Response res = new Response();
		res.setObject(new SurveyImportJobView(job));
		return res;
	}
	
	@RequestMapping(value="importstatus", method=GET)
	public @ResponseBody SurveyImportJobView getSurveyImportStatus() {
		if (surveyImportJob == null) {
			return null;
		} else {
			return new SurveyImportJobView(surveyImportJob);
		}
	}
	
	@RequestMapping(value="changeusergroup/{id}", method=POST)
	public @ResponseBody SurveyView changeSurveyUserGroup(@PathVariable int id, @RequestParam int userGroupId) throws SurveyStoreException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		survey.setUserGroupId(userGroupId);
		surveyManager.save(survey);
		return generateView(survey, false);
	}
	
	@RequestMapping(value = "export/{id}", method=POST)
	public @ResponseBody JobView startExport(@Valid SurveyExportParameters params, BindingResult result) {
		this.surveyBackupJob = null;
		
		String uri = params.getSurveyUri();
//		boolean skipValidation = params.isSkipValidation();
		
		SurveySummary surveySummary = surveyManager.loadSummaryByUri(uri);
		final CollectSurvey loadedSurvey;
		if ( surveySummary.isTemporary() && params.getSurveyType() == SurveyType.TEMPORARY ) {
			loadedSurvey = surveyManager.loadSurvey(surveySummary.getId());
		} else {
			loadedSurvey = surveyManager.getByUri(uri);
		}
		switch(params.getOutputFormat()) {
		case EARTH: {
//			if (skipValidation || collectEarthSurveyValidator.validate(loadedSurvey).isOk()) {
			CollectEarthSurveyExportJob job = jobManager.createJob(CollectEarthSurveyExportJob.class);
			job.setSurvey(loadedSurvey);
			job.setLanguageCode(params.getLanguageCode());
			jobManager.start(job, String.valueOf(loadedSurvey.getId()));
			this.surveyBackupJob = job;
			return new JobView(job);
		}
//			} else {
//				res.setErrorStatus();
//				res.setErrorMessage("survey.validation.errors");
//			}
//		case RDB:
//			RDBPrintJob job = null;
//			job = new RDBPrintJob();
//			job.setSurvey(loadedSurvey);
//			job.setTargetSchemaName(loadedSurvey.getName());
//			job.setRecordManager(recordManager);
//			RecordFilter recordFilter = new RecordFilter(loadedSurvey);
//			job.setRecordFilter(recordFilter);
//			job.setIncludeData(parameters.isIncludeData());
//			job.setDialect(parameters.getRdbDialectEnum());
//			job.setDateTimeFormat(parameters.getRdbDateTimeFormat());
//			job.setTargetSchemaName(parameters.getRdbTargetSchemaName());
//			jobManager.start(job, String.valueOf(loadedSurvey.getId()));
//
//			break;
		case MOBILE:
		default:
			SurveyBackupJob job = jobManager.createJob(SurveyBackupJob.class);
			job.setSurvey(loadedSurvey);
//			surveyBackupJob.setIncludeData(parameters.isIncludeData());
//			surveyBackupJob.setIncludeRecordFiles(parameters.isIncludeUploadedFiles());
			job.setOutputFormat(org.openforis.collect.io.SurveyBackupJob.OutputFormat.valueOf(params.getOutputFormat().name()));
			job.setOutputSurveyDefaultLanguage(ObjectUtils.defaultIfNull(params.getLanguageCode(), loadedSurvey.getDefaultLanguage()));
			jobManager.start(job, String.valueOf(loadedSurvey.getId()));
			this.surveyBackupJob = job;
			return new JobView(job);
		}
	}
	
	@RequestMapping(value="export/{surveyId}/result", method=GET)
	public void downloadCsvExportResult(HttpServletResponse response) throws FileNotFoundException, IOException {
		if (surveyBackupJob != null) {
			File outputFile;
			String outputFileExtension;
			CollectSurvey survey;
			if (surveyBackupJob instanceof CollectEarthSurveyExportJob) {
				CollectEarthSurveyExportJob backupJob = (CollectEarthSurveyExportJob) surveyBackupJob;
				outputFile = backupJob.getOutputFile();
				outputFileExtension = COLLECT_EARTH_PROJECT_FILE_EXTENSION;
				survey = backupJob.getSurvey();
			} else {
				SurveyBackupJob backupJob = (SurveyBackupJob) surveyBackupJob;
				outputFile = backupJob.getOutputFile();
				outputFileExtension = backupJob.getOutputFormat().getOutputFileExtension();
				survey = backupJob.getSurvey();
			}
			String fileName = String.format("%s_%s.%s", survey.getName(), Dates.formatCompactDateTime(survey.getModifiedDate()), outputFileExtension);
			Controllers.writeFileToResponse(response, outputFile, fileName, Controllers.ZIP_CONTENT_TYPE);
		}
	}
	
	private SurveyView generateView(CollectSurvey survey, boolean includeCodeListValues) {
		if (survey == null) {
			return null;
		}
		SurveyViewGenerator viewGenerator = new SurveyViewGenerator(Locale.ENGLISH.getLanguage());
		viewGenerator.setIncludeCodeListValues(includeCodeListValues);
		SurveyView view = viewGenerator.generateView(survey);
		return view;
	}
	
	private Set<UserGroup> getAvailableUserGrups(Integer userId, Integer groupId) {
		if (groupId != null) {
			UserGroup group = userGroupManager.loadById(groupId);
			Set<UserGroup> groups = Collections.singleton(group);
			return groups;
		} else if (userId != null) {
			User availableToUser = userId == null ? null : userManager.loadById(userId);
			List<UserGroup> groups = userGroupManager.findByUser(availableToUser);
			return new HashSet<UserGroup>(groups);
		} else {
			return null;
		}
	}
	
	private File extractIdmFromCEPFile(File surveyFile) {
		try {
			ZipFileExtractor zipFileExtractor = new ZipFileExtractor(surveyFile);
			File idmFile = zipFileExtractor.extract(PLACEMARK_FILE_NAME);
			return idmFile;
		} catch (Exception e) {
			throw new RuntimeException("Error extracting " + PLACEMARK_FILE_NAME + " from cep file", e);
		}
	}
	
	public static class SurveyCreationParameters {
		
		public enum TemplateType {
			BLANK,
			BIOPHYSICAL, 
			COLLECT_EARTH,
			COLLECT_EARTH_IPCC,
			//SOCIOECONOMIC, 
		}

		private String name;
		private TemplateType templateType;
		private String defaultLanguageCode;
		private Integer userGroupId;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public TemplateType getTemplateType() {
			return templateType;
		}

		public void setTemplateType(TemplateType templateType) {
			this.templateType = templateType;
		}

		public String getDefaultLanguageCode() {
			return defaultLanguageCode;
		}
		
		public void setDefaultLanguageCode(String defaultLanguageCode) {
			this.defaultLanguageCode = defaultLanguageCode;
		}
		
		public Integer getUserGroupId() {
			return userGroupId;
		}
		
		public void setUserGroupId(Integer userGroupId) {
			this.userGroupId = userGroupId;
		}
	}
	
	public static class SurveyImportParameters {
		
		private String name;
		private Integer userGroupId;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public Integer getUserGroupId() {
			return userGroupId;
		}
		
		public void setUserGroupId(Integer userGroupId) {
			this.userGroupId = userGroupId;
		}
	}
	
	public static class SurveyExportParameters {
		
		private String surveyUri;
		private Integer surveyId;
		private SurveyType surveyType;
		private OutputFormat outputFormat;
		private String languageCode;
		private boolean skipValidation;
		
		public enum OutputFormat {
			MOBILE, DESKTOP, RDB, EARTH
		}
		
		public String getSurveyUri() {
			return surveyUri;
		}

		public void setSurveyUri(String surveyUri) {
			this.surveyUri = surveyUri;
		}

		public Integer getSurveyId() {
			return surveyId;
		}

		public void setSurveyId(Integer surveyId) {
			this.surveyId = surveyId;
		}
		
		public SurveyType getSurveyType() {
			return surveyType;
		}
		
		public void setSurveyType(SurveyType surveyType) {
			this.surveyType = surveyType;
		}
		
		public OutputFormat getOutputFormat() {
			return outputFormat;
		}
		
		public void setOutputFormat(OutputFormat outputFormat) {
			this.outputFormat = outputFormat;
		}
		
		public String getLanguageCode() {
			return languageCode;
		}
		
		public void setLanguageCode(String languageCode) {
			this.languageCode = languageCode;
		}
		
		public boolean isSkipValidation() {
			return skipValidation;
		}
		
		public void setSkipValidation(boolean skipValidation) {
			this.skipValidation = skipValidation;
		}
	}
	
	public static class SurveyCloneParameters {
		
		public enum SurveyType {
			TEMPORARY, PUBLISHED
		}
		
		private String originalSurveyName;
		private SurveyType originalSurveyType;
		private String newSurveyName;
		
		public String getOriginalSurveyName() {
			return originalSurveyName;
		}
		
		public void setOriginalSurveyName(String originalSurveyName) {
			this.originalSurveyName = originalSurveyName;
		}
		
		public SurveyType getOriginalSurveyType() {
			return originalSurveyType;
		}
		
		public void setOriginalSurveyType(SurveyType originalSurveyType) {
			this.originalSurveyType = originalSurveyType;
		}
		
		public String getNewSurveyName() {
			return newSurveyName;
		}
		
		public void setNewSurveyName(String newSurveyName) {
			this.newSurveyName = newSurveyName;
		}
	}
	
	public static class SurveyImportJobView extends JobView {
		
		private Integer surveyId;

		public SurveyImportJobView(AbstractSurveyRestoreJob job) {
			super(job);
			this.surveyId = job == null || job.getSurvey() == null ? null : job.getSurvey().getId();
		}
		
		public Integer getSurveyId() {
			return surveyId;
		}
	}
	
	public static class SurveyCloneJob extends Job {
		
		private SurveyManager surveyManager;
		
		//input
		private String originalSurveyName;
		private SurveyType originalSurveyType;
		private String newName;
		private User activeUser;
		
		//ouptut
		private CollectSurvey outputSurvey;
		
		public SurveyCloneJob(SurveyManager surveyManager) {
			super();
			this.surveyManager = surveyManager;
		}

		@Override
		protected void buildTasks() throws Throwable {
			addTask(new Task() {
				protected void execute() throws Throwable {
					outputSurvey = surveyManager.duplicateSurveyIntoTemporary(originalSurveyName, 
							originalSurveyType == SurveyType.TEMPORARY, newName, activeUser);
				}
			});
		}
		
		public void setOriginalSurveyName(String originalSurveyName) {
			this.originalSurveyName = originalSurveyName;
		}
		
		public void setOriginalSurveyType(SurveyType originalSurveyType) {
			this.originalSurveyType = originalSurveyType;
		}
		
		public void setNewName(String newName) {
			this.newName = newName;
		}
		
		public void setActiveUser(User activeUser) {
			this.activeUser = activeUser;
		}
		
		public CollectSurvey getOutputSurvey() {
			return outputSurvey;
		}
	}
	
}
