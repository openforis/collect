package org.openforis.collect.web.controller;

import static org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreator.PLACEMARK_FILE_NAME;
import static org.openforis.collect.web.ws.AppWS.MessageType.SURVEYS_UPDATED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
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
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.validation.CollectEarthSurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.manager.validation.SurveyValidator.ValidationParameters;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.view.SurveyView;
import org.openforis.collect.metamodel.view.SurveyViewGenerator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.relational.print.RDBPrintJob;
import org.openforis.collect.relational.print.RDBPrintJob.RdbDialect;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.Files;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.collect.web.controller.CollectJobController.JobView;
import org.openforis.collect.web.controller.SurveyController.SurveyCloneParameters.SurveyType;
import org.openforis.collect.web.manager.SessionRecordProvider;
import org.openforis.collect.web.service.SurveyService;
import org.openforis.collect.web.validator.SimpleSurveyCreationParametersValidator;
import org.openforis.collect.web.validator.SurveyCloneParametersValidator;
import org.openforis.collect.web.validator.SurveyCreationParametersValidator;
import org.openforis.collect.web.validator.SurveyImportParametersValidator;
import org.openforis.collect.web.ws.AppWS;
import org.openforis.collect.web.ws.AppWS.MessageType;
import org.openforis.collect.web.ws.AppWS.SurveyMessage;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.web.Response;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.JobManager;
import org.openforis.concurrency.Task;
import org.openforis.concurrency.Worker.Status;
import org.openforis.concurrency.WorkerStatusChangeEvent;
import org.openforis.concurrency.WorkerStatusChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("/api/survey")
@Scope(SCOPE_SESSION)
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class SurveyController extends BasicController {

	private static final Logger LOG = LogManager.getLogger(SurveyController.class);
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
	@Autowired
	private SessionRecordProvider sessionRecordProvider;
	@Autowired
	private SurveyService surveyService;
	@Autowired
	private AppWS appWS;

	// validators
	@Autowired
	private SurveyCreationParametersValidator surveyCreationParametersValidator;
	@Autowired
	private SimpleSurveyCreationParametersValidator simpleSurveyCreationParametersValidator;
	@Autowired
	private SurveyImportParametersValidator surveyImportParametersValidator;
	@Autowired
	private SurveyCloneParametersValidator surveyCloneParametersValidator;
	@Autowired
	private SurveyValidator surveyValidator;
	@Autowired
	private CollectEarthSurveyValidator collectEarthSurveyValidator;

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

	@RequestMapping(method = GET)
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public @ResponseBody List<?> loadSurveys(@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "groupId", required = false) Integer groupId,
			@RequestParam(value = "full", required = false) boolean fullSurveys,
			@RequestParam(value = "includeCodeListValues", required = false) boolean includeCodeListValues,
			@RequestParam(value = "includeTemporary", required = false) boolean includeTemporary,
			@RequestParam(value = "langCode", required = false, defaultValue = "en") String langCode) throws Exception {
		String languageCode = Locale.ENGLISH.getLanguage();
		if (userId == null) {
			userId = sessionManager.getLoggedUser().getId();
		}
		Set<Integer> groupIds = getAvailableUserGroupIds(userId, groupId);

		List<SurveySummary> publishedSummaries = new ArrayList<SurveySummary>(
				surveyManager.getSurveySummaries(languageCode, userId, groupIds));

		List<SurveySummary> allSummaries = new ArrayList<SurveySummary>(publishedSummaries);

		if (includeTemporary) {
			List<SurveySummary> tempSummaries = surveyManager.loadTemporarySummaries(languageCode, true, userId,
					groupIds);
			allSummaries.addAll(tempSummaries);
		}

		List<Object> views = new ArrayList<Object>(allSummaries.size());
		if (fullSurveys) {
			for (SurveySummary surveySummary : allSummaries) {
				CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveySummary.getId());
				views.add(generateView(survey, includeCodeListValues, langCode));
			}
		} else {
			views.addAll(allSummaries);
		}
		views.sort(Collections.reverseOrder(new BeanComparator<Object>("modifiedDate")));
		return views;
	}

	@RequestMapping(value = "{id}", method = GET)
	public @ResponseBody SurveyView loadSurvey(@PathVariable int id,
			@RequestParam(value = "includeCodeListValues", required = false, defaultValue = "true") boolean includeCodeListValues,
			@RequestParam(value = "langCode", required = false, defaultValue = "en") String langCode) throws Exception {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		return generateView(survey, includeCodeListValues, langCode);
	}

	@RequestMapping(method = POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public @ResponseBody Response createSurvey(@Valid SurveyCreationParameters params, BindingResult bindingResult)
			throws Exception {
		if (bindingResult.hasErrors()) {
			Response res = new Response();
			res.setErrorStatus();
			res.addObject("errors", bindingResult.getFieldErrors());
			return res;
		}
		CollectSurvey survey = surveyService.createNewSurvey(params);

		SurveySummary surveySummary = SurveySummary.createFromSurvey(survey);

		sendSurveysUpdatedMessage();

		Response res = new Response();
		res.setObject(surveySummary);
		return res;
	}

	@RequestMapping(value = "cloneintotemporary/{surveyId}", method = POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public @ResponseBody Response cloneIntoTemporarySurvey(@PathVariable int surveyId) throws Exception {
		Response response = new Response();
		User loggedUser = sessionManager.getLoggedUser();
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		if (survey.isPublished()) {
			String surveyUri = survey.getUri();
			CollectSurvey temporarySurvey = surveyManager.createTemporarySurveyFromPublished(surveyUri, loggedUser);

			sendSurveysUpdatedMessage();

			response.setObject(temporarySurvey.getId());
		} else {
			response.setErrorStatus();
			response.setErrorMessage(String.format("Survey with id %d is not published as expected", surveyId));
		}
		return response;
	}

	@RequestMapping(value = "validatecreation", method = POST)
	public @ResponseBody Response validateSurveyCreationParameters(@Valid SurveyCreationParameters params,
			BindingResult result) {
		return generateFormValidationResponse(result);
	}

	@RequestMapping(value = "publish/{id}", method = POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public @ResponseBody SurveyPublishResult publishSurvey(@PathVariable int id, @RequestParam boolean ignoreWarnings,
			@RequestParam(value = "langCode", required = false, defaultValue = "en") String langCode)
			throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		CollectSurvey publishedSurvey = survey.isPublished() ? surveyManager.getByUri(survey.getUri()) : null;
		SurveyValidator validator = getSurveyValidator(survey);
		ValidationParameters validationParameters = new ValidationParameters();
		validationParameters.setWarningsIgnored(ignoreWarnings);
		SurveyValidationResults results = validator.validateCompatibilityForPublishing(publishedSurvey, survey,
				validationParameters);
		if (results.hasErrors() || results.hasWarnings()) {
			return new SurveyPublishResult(results);
		} else {
			try {
				User activeUser = sessionManager.getLoggedUser();
				surveyManager.publish(survey, activeUser);
				sendSurveyMessage(MessageType.SURVEY_PUBLISHED, survey.getId());
				return new SurveyPublishResult(generateView(survey, false, langCode));
			} catch(Exception e) {
				LOG.error("Error publishing survey: " + e.getMessage(), e);
				throw e;
			}
		}
	}

	@RequestMapping(value = "unpublish/{id}", method = POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public @ResponseBody SurveyView unpublishSurvey(@PathVariable int id,
			@RequestParam(value = "langCode", required = false, defaultValue = "en") String langCode)
			throws SurveyStoreException {
		User activeUser = sessionManager.getLoggedUser();
		CollectSurvey survey = surveyManager.unpublish(id, activeUser);
		sendSurveyMessage(AppWS.MessageType.SURVEY_UNPUBLISHED, id);
		return generateView(survey, false, langCode);
	}

	@RequestMapping(value = "close/{id}", method = POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public @ResponseBody SurveyView closeSurvey(@PathVariable int id,
			@RequestParam(value = "langCode", required = false, defaultValue = "en") String langCode)
			throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		surveyManager.close(survey);
		sendSurveysUpdatedMessage();
		return generateView(survey, false, langCode);
	}

	@RequestMapping(value = "archive/{id}", method = POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public @ResponseBody SurveyView archiveSurvey(@PathVariable int id,
			@RequestParam(value = "langCode", required = false, defaultValue = "en") String langCode)
			throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		surveyManager.archive(survey);
		sendSurveysUpdatedMessage();
		return generateView(survey, false, langCode);
	}

	@RequestMapping(value = "delete/{id}", method = POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public @ResponseBody Response deleteSurvey(@PathVariable int id) throws SurveyImportException {
		surveyManager.deleteSurvey(id);
		sendSurveyMessage(AppWS.MessageType.SURVEY_DELETED, id);
		return new Response();
	}

	@RequestMapping(value = "clone", method = POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public @ResponseBody JobView cloneSurvey(@Valid SurveyCloneParameters params) {
		surveyCloneJob = new SurveyCloneJob(surveyManager);
		surveyCloneJob.setOriginalSurveyName(params.originalSurveyName);
		surveyCloneJob.setNewName(params.newSurveyName);
		surveyCloneJob.setOriginalSurveyType(params.originalSurveyType);
		surveyCloneJob.setActiveUser(sessionManager.getLoggedUser());
		surveyCloneJob.addStatusChangeListener(new WorkerStatusChangeListener() {
			public void statusChanged(WorkerStatusChangeEvent event) {
				if (event.getTo() == Status.COMPLETED)
					sendSurveysUpdatedMessage();
			}
		});
		jobManager.start(surveyCloneJob);
		return new JobView(surveyCloneJob);
	}

	@RequestMapping(value = "cloned/id", method = GET)
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

	@RequestMapping(value = "validate/clone", method = POST)
	public @ResponseBody Response validateSurveyCloneParameters(@Valid SurveyCloneParameters params,
			BindingResult result) {
		return generateFormValidationResponse(result);
	}

	@RequestMapping(value = "prepareimport", method = POST, consumes = MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody Response prepareSurveyImport(@RequestParam("file") MultipartFile multipartFile) {
		Response response = new Response();
		try {
			String fileName = multipartFile.getOriginalFilename();
			File tempFile = Files.writeToTempFile(multipartFile.getInputStream(), multipartFile.getOriginalFilename(),
					"ofc_csv_data_import");
			String extension = FilenameUtils.getExtension(fileName);
	
			this.uploadedSurveyFile = tempFile;
	
			if (surveyBackupInfoExtractorJob != null && surveyBackupInfoExtractorJob.isRunning()) {
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
	
			if (surveyBackupInfoExtractorJob.isCompleted()) {
				uploadedSurveyInfo = surveyBackupInfoExtractorJob.getInfo();
				response.addObject("surveyBackupInfo", uploadedSurveyInfo);
				SurveySummary existingSummary = surveyManager.loadSummaryByUri(uploadedSurveyInfo.getSurveyUri());
				response.addObject("importingIntoExistingSurvey", existingSummary != null);
				response.addObject("existingSurveyUserGroupId",
						existingSummary == null ? null : existingSummary.getUserGroupId());
			} else {
				response.setErrorStatus();
				response.setErrorMessage(surveyBackupInfoExtractorJob.getErrorMessage());
			}
		} catch(Exception e) {
			FormattedMessage formattedMessage = new FormattedMessage("Error preparing survey import: %s", e.getMessage());
			LOG.error(formattedMessage, e);
			response.setErrorStatus();
			response.setErrorMessage(formattedMessage.toString());
		}
		return response;
	}

	@RequestMapping(value = "validateimport", method = POST)
	public @ResponseBody Response validateSurveyImportParameters(@Valid SurveyImportParameters params,
			BindingResult result) {
		return generateFormValidationResponse(result);
	}

	@RequestMapping(value = "startimport", method = POST)
	public @ResponseBody Response startSurveyFileImport(@Valid SurveyImportParameters params,
			BindingResult bindingResult) {
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
		if (Files.XML_FILE_EXTENSION.equalsIgnoreCase(uploadedFileNameExtension)) {
			job = jobManager.createJob(XMLSurveyRestoreJob.class);
		} else if (COLLECT_EARTH_PROJECT_FILE_EXTENSION.equalsIgnoreCase(uploadedFileNameExtension)) {
			job = jobManager.createJob(CESurveyRestoreJob.class);
		} else {
			job = jobManager.createJob(SurveyRestoreJob.class);
		}
		job.setFile(this.uploadedSurveyFile);
		job.setSurveyName(surveyName);
		job.setSurveyUri(uploadedSurveyInfo == null ? null : uploadedSurveyInfo.getSurveyUri());
		job.setUserGroup(userGroup);
		job.setRestoreIntoPublishedSurvey(false);
		job.setValidateSurvey(false);
		job.setActiveUser(sessionManager.getLoggedUser());

		// on job complete, send survey created message to WS
		job.addStatusChangeListener(new WorkerStatusChangeListener() {
			public void statusChanged(WorkerStatusChangeEvent event) {
				if (event.getTo() == Status.COMPLETED) {
					sendSurveysUpdatedMessage();
				}
			}
		});

		jobManager.start(job);
		this.surveyImportJob = job;
		Response res = new Response();
		res.setObject(new SurveyImportJobView(job));
		return res;
	}

	@RequestMapping(value = "importstatus", method = GET)
	public @ResponseBody SurveyImportJobView getSurveyImportStatus() {
		if (surveyImportJob == null) {
			return null;
		} else {
			return new SurveyImportJobView(surveyImportJob);
		}
	}

	@RequestMapping(value = "{surveyName}/changeusergroup", method = POST)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public @ResponseBody SurveySummary changeSurveyUserGroup(@PathVariable String surveyName,
			@RequestParam int userGroupId) throws SurveyStoreException {
		SurveySummary surveySummary = surveyManager.updateUserGroup(surveyName, userGroupId);
		sendSurveysUpdatedMessage();
		return surveySummary;
	}

	@RequestMapping(value = "export/{id}", method = POST)
	public @ResponseBody JobView startExport(@Valid SurveyExportParameters params, BindingResult result) {
		this.surveyBackupJob = null;

		String uri = params.getSurveyUri();
//		boolean skipValidation = params.isSkipValidation();

		SurveySummary surveySummary = surveyManager.loadSummaryByUri(uri);
		final CollectSurvey loadedSurvey;
		if (surveySummary.isTemporary() && params.getSurveyType() == SurveyType.TEMPORARY) {
			loadedSurvey = surveyManager.loadSurvey(surveySummary.getId());
		} else {
			loadedSurvey = surveyManager.getByUri(uri);
		}
		switch (params.getOutputFormat()) {
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
		case RDB: {
			RDBPrintJob job = jobManager.createJob(RDBPrintJob.class);
			job.setSurvey(loadedSurvey);
			job.setRecordFilter(new RecordFilter(loadedSurvey));
			job.setIncludeData(params.isIncludeData());
			job.setDialect(params.getRdbDialectEnum());
			job.setDateTimeFormat(params.getRdbDateTimeFormat());
			job.setTargetSchemaName(params.getRdbTargetSchemaName());
			jobManager.start(job, String.valueOf(loadedSurvey.getId()));
			this.surveyBackupJob = job;
			return new JobView(job);
		}
		case MOBILE:
		default:
			SurveyBackupJob job = jobManager.createJob(SurveyBackupJob.class);
			job.setSurvey(loadedSurvey);
//			surveyBackupJob.setIncludeData(parameters.isIncludeData());
//			surveyBackupJob.setIncludeRecordFiles(parameters.isIncludeUploadedFiles());
			job.setOutputFormat(
					org.openforis.collect.io.SurveyBackupJob.OutputFormat.valueOf(params.getOutputFormat().name()));
			job.setOutputSurveyDefaultLanguage(
					StringUtils.defaultIfEmpty(params.getLanguageCode(), loadedSurvey.getDefaultLanguage()));
			jobManager.start(job, String.valueOf(loadedSurvey.getId()));
			this.surveyBackupJob = job;
			return new JobView(job);
		}
	}

	@RequestMapping(value = "export/{surveyId}/result", method = GET)
	public void downloadExportResult(HttpServletResponse response) throws FileNotFoundException, IOException {
		if (surveyBackupJob != null) {
			File outputFile;
			String outputFileExtension;
			CollectSurvey survey;
			String projectName;
			if (surveyBackupJob instanceof CollectEarthSurveyExportJob) {
				CollectEarthSurveyExportJob backupJob = (CollectEarthSurveyExportJob) surveyBackupJob;
				outputFile = backupJob.getOutputFile();
				outputFileExtension = COLLECT_EARTH_PROJECT_FILE_EXTENSION;
				survey = backupJob.getSurvey();
				projectName = survey.getName();
			} else if (surveyBackupJob instanceof RDBPrintJob) {
				RDBPrintJob rdbPrintJob = (RDBPrintJob) surveyBackupJob;
				outputFile = rdbPrintJob.getOutputFile();
				outputFileExtension = FilenameUtils.getExtension(outputFile.getName());
				survey = rdbPrintJob.getSurvey();
				projectName = survey.getName();
			} else {
				SurveyBackupJob backupJob = (SurveyBackupJob) surveyBackupJob;
				outputFile = backupJob.getOutputFile();
				outputFileExtension = backupJob.getOutputFormat().getOutputFileExtension();
				survey = backupJob.getSurvey();
				projectName = survey.getName();
				if (backupJob.getOutputFormat() == SurveyBackupJob.OutputFormat.MOBILE) {
					projectName += "_" + backupJob.getOutputSurveyDefaultLanguage();
				}
			}
			String fileName = String.format("%s_%s.%s", projectName,
					Dates.formatCompactDateTime(survey.getModifiedDate()), outputFileExtension);
			Controllers.writeFileToResponse(response, outputFile, fileName, MediaTypes.ZIP_CONTENT_TYPE);
		}
	}

	private SurveyView generateView(CollectSurvey survey, boolean includeCodeListValues, String langCode) {
		if (survey == null) {
			return null;
		}
		SurveyViewGenerator viewGenerator = new SurveyViewGenerator(langCode);
		viewGenerator.setIncludeCodeListValues(includeCodeListValues);
		UserInGroup userInSurveyGroup = userGroupManager.findUserInGroupOrDescendants(survey.getUserGroupId(),
				sessionManager.getLoggedUser().getId());
		UserGroup userGroup = userInSurveyGroup == null ? null
				: userGroupManager.loadById(userInSurveyGroup.getGroupId());
		SurveyView view = viewGenerator.generateView(survey, userGroup,
				userInSurveyGroup == null ? null : userInSurveyGroup.getRole());
		return view;
	}

	private Set<Integer> getAvailableUserGroupIds(Integer userId, Integer groupId) {
		if (groupId != null) {
			return Collections.singleton(groupId);
		} else if (userId != null) {
			User availableToUser = userId == null ? null : userManager.loadById(userId);
			List<UserGroup> groups = userGroupManager.findAllRelatedUserGroups(availableToUser);
			List<Integer> groupIds = CollectionUtils.project(groups, "id");
			return new HashSet<Integer>(groupIds);
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

	private void sendSurveysUpdatedMessage() {
		appWS.sendMessage(SURVEYS_UPDATED, 500); // delay to allow transaction commit
	}

	private void sendSurveyMessage(MessageType surveydMessageType, int surveyId) {
		appWS.sendMessage(new SurveyMessage(surveydMessageType, surveyId), 500);
		sessionRecordProvider.clearRecords(surveyId);
		sendSurveysUpdatedMessage();
	}

	
	private SurveyValidator getSurveyValidator(CollectSurvey survey) {
		return survey.getTarget() == SurveyTarget.COLLECT_EARTH ? collectEarthSurveyValidator : surveyValidator;
	}

	public static class SurveyCreationParameters {

		public enum TemplateType {
			BLANK, BIOPHYSICAL, COLLECT_EARTH, COLLECT_EARTH_IPCC,
			// SOCIOECONOMIC,
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
		// RDB export params
		private boolean includeData;
		private String rdbDialect;
		private String rdbDateTimeFormat;
		private String rdbTargetSchemaName;

		public enum OutputFormat {
			MOBILE, DESKTOP, RDB, EARTH
		}

		public String getSurveyUri() {
			return surveyUri;
		}

		public RdbDialect getRdbDialectEnum() {
			return rdbDialect == null ? null : RdbDialect.valueOf(rdbDialect);
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

		public boolean isIncludeData() {
			return includeData;
		}

		public void setIncludeData(boolean includeData) {
			this.includeData = includeData;
		}

		public String getRdbDateTimeFormat() {
			return rdbDateTimeFormat;
		}

		public void setRdbDateTimeFormat(String rdbDateTimeFormat) {
			this.rdbDateTimeFormat = rdbDateTimeFormat;
		}

		public String getRdbTargetSchemaName() {
			return rdbTargetSchemaName;
		}

		public void setRdbTargetSchemaName(String rdbTargetSchemaName) {
			this.rdbTargetSchemaName = rdbTargetSchemaName;
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

		// input
		private String originalSurveyName;
		private SurveyType originalSurveyType;
		private String newName;
		private User activeUser;

		// ouptut
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

	public class SurveyPublishResult {
		private SurveyView survey;
		private SurveyValidationResults validationResult;

		public SurveyPublishResult(SurveyView survey) {
			super();
			this.survey = survey;
		}

		public SurveyPublishResult(SurveyValidationResults validationResult) {
			super();
			this.validationResult = validationResult;
		}

		public SurveyView getSurvey() {
			return survey;
		}

		public SurveyValidationResults getValidationResult() {
			return validationResult;
		}
	}
}
