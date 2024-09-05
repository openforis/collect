package org.openforis.collect.web.controller;

import static org.openforis.collect.web.ws.AppWS.MessageType.SURVEYS_UPDATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.event.EventListenerToList;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.EventProducer.EventProducerContext;
import org.openforis.collect.event.EventQueue;
import org.openforis.collect.event.RecordDeletedEvent;
import org.openforis.collect.event.RecordStep;
import org.openforis.collect.event.RecordTransaction;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.BulkRecordMoveJob;
import org.openforis.collect.io.data.CSVDataExportJob;
import org.openforis.collect.io.data.CSVDataImportJob;
import org.openforis.collect.io.data.CSVDataImportJob.CSVDataImportInput;
import org.openforis.collect.io.data.DataImportSummary;
import org.openforis.collect.io.data.DataRestoreJob;
import org.openforis.collect.io.data.DataRestoreSummaryJob;
import org.openforis.collect.io.data.RandomRecordsGenerationJob;
import org.openforis.collect.io.data.RecordProvider;
import org.openforis.collect.io.data.RecordProviderConfiguration;
import org.openforis.collect.io.data.RecordsCountJob;
import org.openforis.collect.io.data.TransactionalCSVDataImportJob;
import org.openforis.collect.io.data.TransactionalDataRestoreJob;
import org.openforis.collect.io.data.XMLParsingRecordProvider;
import org.openforis.collect.io.data.csv.CSVDataExportParameters;
import org.openforis.collect.io.data.csv.CSVDataExportParametersBase;
import org.openforis.collect.io.data.csv.CSVDataExportParametersBase.OutputFormat;
import org.openforis.collect.io.data.csv.CSVDataImportSettings;
import org.openforis.collect.io.data.proxy.DataImportStatusProxy;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RandomRecordGenerator;
import org.openforis.collect.manager.RecordAccessControlManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordGenerator;
import org.openforis.collect.manager.RecordGenerator.NewRecordParameters;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordPromoteException;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.ValidationReportJob;
import org.openforis.collect.manager.ValidationReportJob.Input;
import org.openforis.collect.manager.ValidationReportJob.ReportType;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.model.proxy.BasicUserProxy;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.model.proxy.RecordSummaryProxy;
import org.openforis.collect.persistence.MissingRecordKeyException;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.remoting.service.dataimport.DataImportSummaryProxy;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.Files;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.collect.utils.Proxies;
import org.openforis.collect.web.controller.CollectJobController.JobView;
import org.openforis.collect.web.controller.RecordStatsGenerator.RecordsStats;
import org.openforis.collect.web.manager.SessionRecordProvider;
import org.openforis.collect.web.session.SessionState;
import org.openforis.collect.web.ws.AppWS;
import org.openforis.commons.web.HttpResponses;
import org.openforis.commons.web.Response;
import org.openforis.concurrency.Worker;
import org.openforis.concurrency.WorkerStatusChangeEvent;
import org.openforis.concurrency.WorkerStatusChangeListener;
import org.openforis.concurrency.proxy.JobProxy;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
@Scope(SCOPE_SESSION)
@RequestMapping("api")
public class RecordController extends BasicController implements Serializable {

	private static final long serialVersionUID = 1L;

	// private static Logger LOG = Logger.getLogger(DataController.class);

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private RecordFileManager recordFileManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CollectSurveyContext surveyContext;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private RandomRecordGenerator randomRecordGenerator;
	@Autowired
	private RecordGenerator recordGenerator;
	@Autowired
	private RecordSessionManager sessionManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserGroupManager userGroupManager;
	@Autowired
	private CollectJobManager jobManager;
	@Autowired
	private SessionRecordProvider sessionRecordProvider;
	@Autowired
	private RecordStatsGenerator recordStatsGenerator;
	@Autowired
	private transient EventQueue eventQueue;
	@Autowired
	private AppWS appWS;

	private CSVDataExportJob csvDataExportJob;
	private SurveyBackupJob fullBackupJob;
	private DataRestoreSummaryJob dataRestoreSummaryJob;
	private CSVDataImportJob csvDataImportJob;
	private ValidationReportJob validationReportJob;

	@RequestMapping(value = "survey/{surveyId}/data/records/{recordId}/binary_data.json", method = GET)
	public @ResponseBody Map<String, Object> loadData(@PathVariable("surveyId") int surveyId,
			@PathVariable("recordId") int recordId, @RequestParam(value = "step") Integer stepNumber) throws Exception {
		stepNumber = getStepNumberOrDefault(stepNumber);
		CollectSurvey survey = surveyManager.getById(surveyId);
		byte[] data = recordManager.loadBinaryData(survey, recordId, Step.valueOf(stepNumber));
		byte[] encoded = Base64.encodeBase64(data);
		String result = new String(encoded);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("data", result);

		return map;
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/count.json", method = GET)
	public @ResponseBody int getCount(@PathVariable("surveyId") int surveyId,
			@RequestParam(value = "rootEntityDefinitionId", required = false) Integer rootEntityDefinitionId,
			@RequestParam(value = "step", required = false) Integer stepNumber) throws Exception {
		CollectSurvey survey = surveyManager.getById(surveyId);
		RecordFilter filter = createRecordFilter(survey, sessionManager.getLoggedUser(), userGroupManager,
				rootEntityDefinitionId, false);
		if (stepNumber != null) {
			filter.setStepGreaterOrEqual(Step.valueOf(stepNumber));
		}
		int count = recordManager.countRecords(filter);
		return count;
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/summary", method = GET)
	public @ResponseBody Map<String, Object> loadRecordSummaries(@PathVariable("surveyId") int surveyId,
			@Valid RecordSummarySearchParameters params) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		User user = loadUser(params.getUserId(), params.getUsername());

		Map<String, Object> result = new HashMap<String, Object>();
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDefinition = params.getRootEntityName() == null
				? schema.getFirstRootEntityDefinition()
				: schema.getRootEntityDefinition(params.getRootEntityName());

		RecordFilter filter = createRecordFilter(survey, user, userGroupManager, rootEntityDefinition.getId(), false);

		filter.setKeyValues(params.getKeyValues());
		filter.setCaseSensitiveKeyValues(params.isCaseSensitiveKeyValues());

		if (CollectionUtils.isEmpty(filter.getQualifiers())) {
			// filter by qualifiers only if not already done by user group qualifiers
			filter.setQualifiers(params.getQualifierValues());
		}
		filter.setSummaryValues(params.getSummaryValues());
		if (filter.getOwnerIds() == null && params.getOwnerIds() != null && params.getOwnerIds().length > 0) {
			filter.setOwnerIds(Arrays.asList(params.getOwnerIds()));
		}
		filter.setOffset(params.getOffset());
		filter.setMaxNumberOfRecords(params.getMaxNumberOfRows());

		// load summaries
		List<CollectRecordSummary> summaries = params.isFullSummary()
				? recordManager.loadFullSummaries(filter, params.getSortFields())
				: recordManager.loadSummaries(filter, params.getSortFields());
		result.put("records", toProxies(summaries));

		// count total records
		int count = recordManager.countRecords(filter);
		result.put("count", count);

		if (params.isIncludeOwners()) {
			Set<User> owners = recordManager.loadDistinctOwners(
					createRecordFilter(survey, user, userGroupManager, rootEntityDefinition.getId(), false));
			Set<BasicUserProxy> ownerProxies = Proxies.fromSet(owners, BasicUserProxy.class);
			result.put("owners", ownerProxies);
		}

		return result;
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/{recordId}", method = GET, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody RecordProxy loadRecord(@PathVariable("surveyId") int surveyId,
			@PathVariable("recordId") int recordId, @RequestParam(value = "step", required = false) Integer stepNumber,
			@RequestParam(value = "lock", required = false, defaultValue = "false") boolean lock)
			throws RecordPersistenceException {
		CollectSurvey survey = surveyManager.getById(surveyId);
		Step step = stepNumber == null ? null : Step.valueOf(stepNumber);
		User user = sessionManager.getLoggedUser();
		UserGroup surveyUserGrup = survey.getUserGroup();
		UserInGroup userInGroup = userGroupManager.findUserInGroupOrDescendants(surveyUserGrup.getId(), user.getId());
		if (userInGroup == null) {
			throw new IllegalArgumentException(
					String.format("User %s is not allowed to load record with id %d", user.getUsername(), recordId));
		}
		CollectRecordSummary recordSummary = recordManager.loadUniqueRecordSummary(survey, recordId);
		if ((user.hasRole(UserRole.ENTRY_LIMITED) || userInGroup.getRole() == UserGroupRole.DATA_CLEANER_LIMITED)
				&& (recordSummary.getOwner() == null || !user.getId().equals(recordSummary.getOwner().getId()))) {
			throw new IllegalStateException(
					String.format("User '%s' (entry_limited) cannot access record with ID %d: he doesn't own it.",
							user.getUsername(), recordId));
		}
		CollectRecord record = lock
				? recordManager.checkout(survey, user, recordId, step, sessionManager.getSessionState().getSessionId(),
						true)
				: recordManager.load(survey, recordId, step);
		sessionRecordProvider.putRecord(record);
		return toProxy(record);
	}

	@RequestMapping(value = "survey/{surveyId}/data/update/records/{recordId}", method = POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody Response updateOwner(@PathVariable("surveyId") int surveyId,
			@PathVariable("recordId") int recordId, @RequestBody Map<String, String> body)
			throws RecordLockedException, MultipleEditException {
		String ownerIdStr = body.get("ownerId");
		Integer ownerId = ownerIdStr == null ? null : Integer.parseInt(ownerIdStr);
		CollectSurvey survey = surveyManager.getById(surveyId);
		SessionState sessionState = sessionManager.getSessionState();
		recordManager.assignOwner(survey, recordId, ownerId, sessionState.getUser(), sessionState.getSessionId());
		return new Response();
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/promote/{recordId}", method = POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody Response promoteRecord(@PathVariable("surveyId") int surveyId,
			@PathVariable("recordId") int recordId) throws MissingRecordKeyException, RecordPromoteException {
		CollectSurvey survey = surveyManager.getById(surveyId);
		CollectRecord record = recordManager.load(survey, recordId);
		recordManager.promote(record, sessionManager.getLoggedUser(), true);
		return new Response();
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/demote/{recordId}", method = POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody Response demoteRecord(@PathVariable("surveyId") int surveyId,
			@PathVariable("recordId") int recordId) throws RecordPersistenceException {
		CollectSurvey survey = surveyManager.getById(surveyId);
		recordManager.demote(survey, recordId, sessionManager.getLoggedUser());
		return new Response();
	}

	@RequestMapping(value = "survey/{surveyId}/data/move/records", method = POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody JobProxy moveRecords(@PathVariable("surveyId") int surveyId, @RequestParam String fromStep,
			@RequestParam boolean promote) {
		BulkRecordMoveJob job = jobManager.createJob(BulkRecordMoveJob.class);
		SessionState sessionState = sessionManager.getSessionState();
		User loggedUser = sessionState.getUser();
		CollectSurvey survey = surveyManager.getById(surveyId);
		EntityDefinition rootEntityDef = survey.getSchema().getFirstRootEntityDefinition();
		job.setSurvey(survey);
		job.setRootEntity(rootEntityDef.getName());
		job.setPromote(promote);
		job.setFromStep(Step.valueOf(fromStep));
		job.setUser(loggedUser);
		job.setRecordMovedCallback(new BulkRecordMoveJob.Callback() {
			@Override
			public void recordMoved(CollectRecord record) {
				if (promote) {
					publishRecordPromotedEvents(record, loggedUser.getUsername());
				} else {
					publishRecordDeletedEvent(record, RecordStep.valueOf(fromStep), loggedUser.getUsername());
				}
			}
		});
		sessionRecordProvider.clearRecords(surveyId);
		jobManager.startSurveyJob(job);
		return new JobProxy(job);
	}

	@Transactional
	@RequestMapping(value = "survey/{surveyId}/data/records", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody RecordProxy newRecord(@PathVariable("surveyId") int surveyId,
			@RequestBody NewRecordParameters params) throws RecordPersistenceException {
		User user = sessionManager.getLoggedUser();
		if (user == null) {
			user = loadUser(params.getUserId(), params.getUsername());
		}
		CollectSurvey survey = params.isPreview() ? surveyManager.loadSurvey(surveyId)
				: surveyManager.getById(surveyId);
		params.setRootEntityName(ObjectUtils.defaultIfNull(params.getRootEntityName(),
				survey.getSchema().getFirstRootEntityDefinition().getName()));
		Integer latestVersionId = survey.getLatestVersion() != null ? survey.getLatestVersion().getId() : null;
		params.setVersionId(ObjectUtils.defaultIfNull(params.getVersionId(), latestVersionId));
		params.setUserId(user.getId());
		CollectRecord record = recordGenerator.generate(survey, params);
		sessionRecordProvider.putRecord(record);
		return toProxy(record);
	}

	@Transactional
	@RequestMapping(value = "survey/{surveyId}/data/records/random", method = POST, consumes = APPLICATION_JSON_VALUE)
	public @ResponseBody RecordProxy createRandomRecord(@PathVariable("surveyId") int surveyId,
			@RequestBody NewRecordParameters params) throws RecordPersistenceException {
		CollectRecord record = randomRecordGenerator.generate(surveyId, params);
		return toProxy(record);
	}

	@RequestMapping(value = "survey/{surveyId}/data/records", method = DELETE, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody Response deleteRecord(@PathVariable("surveyId") int surveyId,
			@Valid RecordDeleteParameters params) throws RecordPersistenceException {
		if (canDeleteRecords(surveyId, Sets.newHashSet(params.getRecordIds()))) {
			CollectSurvey survey = surveyManager.getById(surveyId);
			for (Integer recordId : params.getRecordIds()) {
				CollectRecord record = recordManager.load(survey, recordId);
				recordFileManager.deleteAllFiles(record);
				recordManager.delete(recordId);
				publishRecordDeletedEvent(record, record.getStep().toRecordStep(),
						sessionManager.getLoggedUser().getUsername());
			}
			return new Response();
		} else {
			Response response = new Response();
			response.setErrorStatus();
			response.setErrorMessage("Cannot delete some of the specified records: unsufficient user privilegies");
			return response;
		}
	}

	@RequestMapping(value = "survey/{surveyId}/data/import/records/summary", method = POST, consumes = MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JobView startRecordImportSummaryJob(@PathVariable("surveyId") int surveyId,
			@RequestParam("file") MultipartFile multipartFile, @RequestParam String rootEntityName) throws IOException {
		File tempFile = null;
		try {
			tempFile = File.createTempFile("ofc_data_restore", ".collect-data");
			tempFile.deleteOnExit();
			FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), tempFile);

			CollectSurvey survey = surveyManager.getById(surveyId);

			DataRestoreSummaryJob job = jobManager.createJob(DataRestoreSummaryJob.class);
			job.setUser(sessionManager.getLoggedUser());
			job.setFullSummary(true);
			job.setFile(tempFile);
			job.setPublishedSurvey(survey);
			job.setCloseRecordProviderOnComplete(false);
			job.setDeleteInputFileOnDestroy(true);

			jobManager.start(job);
			this.dataRestoreSummaryJob = job;
			return new JobView(job);
		} catch (Exception e) {
			FileUtils.deleteQuietly(tempFile);
			throw e;
		}
	}

	@RequestMapping(value = "survey/{surveyId}/data/import/records/summary", method = GET)
	public @ResponseBody DataImportSummaryProxy downloadRecordImportSummary(@PathVariable("surveyId") int surveyId)
			throws IOException {
		if (this.dataRestoreSummaryJob == null || !this.dataRestoreSummaryJob.isCompleted()) {
			throw new IllegalStateException(
					"Data restore summary not generated or an error occurred during the generation");
		}
		DataImportSummary summary = this.dataRestoreSummaryJob.getSummary();
		return new DataImportSummaryProxy(summary, sessionManager.getSessionState().getLocale());
	}

	@RequestMapping(value = "survey/{surveyId}/data/import/records", method = POST)
	public @ResponseBody JobView startRecordImport(@PathVariable("surveyId") int surveyId,
			@RequestParam List<Integer> entryIdsToImport, @RequestParam(defaultValue = "true") boolean validateRecords)
			throws IOException {
		RecordProvider recordProvider = dataRestoreSummaryJob.getRecordProvider();
		if (recordProvider instanceof XMLParsingRecordProvider)
			((XMLParsingRecordProvider) recordProvider).setInitializeRecords(true);
		recordProvider.setConfiguration(new RecordProviderConfiguration(true));
		DataRestoreJob job = jobManager.createJob(TransactionalDataRestoreJob.class);
		job.setFile(dataRestoreSummaryJob.getFile());
		job.setUser(sessionManager.getLoggedUser());
		job.setValidateRecords(validateRecords);
		job.setRecordProvider(recordProvider);
		job.setPackagedSurvey(dataRestoreSummaryJob.getPackagedSurvey());
		job.setPublishedSurvey(dataRestoreSummaryJob.getPublishedSurvey());
		job.setEntryIdsToImport(entryIdsToImport);
		job.setRecordFilesToBeDeleted(dataRestoreSummaryJob.getSummary().getConflictingRecordFiles(entryIdsToImport));
		job.setRestoreUploadedFiles(true);
		jobManager.start(job);
		return new JobView(job);
	}

	@RequestMapping(value = "survey/{surveyId}/data/csvimport/records", method = POST, consumes = MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JobView startCsvDataImportJob(@PathVariable("surveyId") int surveyId,
			@RequestParam("file") MultipartFile multipartFile, @RequestParam String rootEntityName,
			@RequestParam String importType, @RequestParam String steps,
			@RequestParam(required = false) Integer entityDefinitionId,
			@RequestParam(required = false) boolean validateRecords,
			@RequestParam(required = false) boolean deleteEntitiesBeforeImport,
			@RequestParam(required = false) String newRecordVersionName) throws IOException {
		File file = Files.writeToTempFile(multipartFile.getInputStream(), multipartFile.getOriginalFilename(),
				"ofc_csv_data_import");
		CollectSurvey survey = surveyManager.getById(surveyId);
		CSVDataImportJob job = jobManager.createJob(TransactionalCSVDataImportJob.class);
		CSVDataImportSettings settings = new CSVDataImportSettings();
		settings.setDeleteExistingEntities(deleteEntitiesBeforeImport);
		settings.setRecordValidationEnabled(validateRecords);
		settings.setInsertNewRecords("newRecords".equals(importType));
		settings.setNewRecordVersionName(newRecordVersionName);
		CSVDataImportInput input = new CSVDataImportInput(file, survey, fromStepNames(steps), entityDefinitionId,
				settings);
		job.setInput(input);
		jobManager.start(job);
		this.csvDataImportJob = job;
		return new JobView(job);
	}

	@RequestMapping(value = "survey/{surveyId}/data/csvimport/records", method = GET)
	public @ResponseBody DataImportStatusProxy getCsvDataImportStatus(@PathVariable("surveyId") int surveyId) {
		return new DataImportStatusProxy(csvDataImportJob);
	}

	private Step[] fromStepNames(String stepNamesStr) {
		String[] stepNames = stepNamesStr.split(",");
		Step[] steps = new Step[stepNames.length];
		for (int i = 0; i < stepNames.length; i++) {
			steps[i] = Step.valueOf(stepNames[i]);
		}
		return steps;
	}

	@RequestMapping(value = "survey/{survey_id}/data/records/{record_id}/steps/{step}/content/csv/data.zip", method = GET, produces = MediaTypes.ZIP_CONTENT_TYPE)
	public void exportRecordToCsv(@PathVariable(value = "survey_id") int surveyId,
			@PathVariable(value = "record_id") int recordId, @PathVariable(value = "step") int stepNumber,
			HttpServletResponse response) throws RecordPersistenceException, IOException {
		exportRecord(surveyId, recordId, stepNumber, OutputFormat.CSV, response);
	}

	@RequestMapping(value = "survey/{survey_id}/data/records/{record_id}/steps/{step}/content/xlsx/data.zip", method = GET, produces = MediaTypes.ZIP_CONTENT_TYPE)
	public void exportRecordToExcel(@PathVariable(value = "survey_id") int surveyId,
			@PathVariable(value = "record_id") int recordId, @PathVariable(value = "step") int stepNumber,
			HttpServletResponse response) throws RecordPersistenceException, IOException {
		exportRecord(surveyId, recordId, stepNumber, OutputFormat.XLSX, response);
	}

	public void exportRecord(int surveyId, int recordId, int stepNumber, OutputFormat outputFormat,
			HttpServletResponse response) throws RecordPersistenceException, IOException {
		CollectSurvey survey = surveyManager.getById(surveyId);
		CollectRecord record = recordManager.load(survey, recordId);
		RecordAccessControlManager accessControlManager = new RecordAccessControlManager();
		if (accessControlManager.canEdit(sessionManager.getLoggedUser(), record)) {
			CSVDataExportJob job = jobManager.createJob(CSVDataExportJob.class);
			job.setSurvey(survey);
			CSVDataExportParameters parameters = new CSVDataExportParameters();
			RecordFilter recordFilter = createRecordFilter(survey, sessionManager.getLoggedUser(), userGroupManager,
					null, false);
			recordFilter.setRecordId(recordId);
			recordFilter.setStepGreaterOrEqual(Step.valueOf(stepNumber));
			parameters.setRecordFilter(recordFilter);
			parameters.setAlwaysGenerateZipFile(true);
			parameters.setOutputFormat(outputFormat);
			job.setParameters(parameters);
			File outputFile = File.createTempFile("record_export", ".zip");
			job.setOutputFile(outputFile);
			jobManager.startSurveyJob(job, false);
			if (job.isCompleted()) {
				String fileName = String.format("record_data_%d_%s.zip", recordId, Dates.formatDate(new Date()));
				Controllers.writeFileToResponse(response, outputFile, fileName, MediaTypes.ZIP_CONTENT_TYPE);
			} else if (job.getLastException() != null) {
				throw new RuntimeException(job.getLastException());
			} else {
				throw new RuntimeException("Error exporting record");
			}
		}
	}

	@RequestMapping(value = "data/records/{recordId}/surveyId", method = GET)
	public @ResponseBody int loadSurveyId(@PathVariable("recordId") int recordId) {
		return recordManager.loadSurveyId(recordId);
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/startcsvexport", method = POST)
	public @ResponseBody JobView startCsvDataExportJob(@PathVariable("surveyId") int surveyId,
			@RequestBody CSVExportParametersForm parameters) throws IOException {
		User user = sessionManager.getLoggedUser();
		CollectSurvey survey = surveyManager.getById(surveyId);

		csvDataExportJob = jobManager.createJob(CSVDataExportJob.class);
		csvDataExportJob.setSurvey(survey);

		csvDataExportJob.setOutputFile(File.createTempFile("collect-csv-data-export", ".zip"));

		CSVDataExportParameters exportParameters = parameters.toExportParameters(survey, user, userGroupManager);
		csvDataExportJob.setParameters(exportParameters);

		jobManager.start(csvDataExportJob);

		return new JobView(csvDataExportJob);
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/currentcsvexport", method = GET)
	public @ResponseBody JobView getCsvDataExportJob(HttpServletResponse response) {
		if (csvDataExportJob == null) {
			HttpResponses.setNoContentStatus(response);
			return null;
		} else {
			return new JobView(csvDataExportJob);
		}
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/csvexportresult.zip", method = GET)
	public void downloadCsvExportResult(HttpServletResponse response) throws FileNotFoundException, IOException {
		File file = csvDataExportJob.getOutputFile();
		RecordFilter recordFilter = csvDataExportJob.getParameters().getRecordFilter();
		CollectSurvey survey = recordFilter.getSurvey();
		String surveyName = survey.getName();
		CSVDataExportParameters parameters = csvDataExportJob.getParameters();
		String outputFormat = parameters.getOutputFormat().name().toLowerCase(Locale.ENGLISH);
		String step = parameters.getRecordFilter().getStepGreaterOrEqual().name();
		String fileName = String.format("collect-%s-data-export-%s-%s-%s.zip", outputFormat, surveyName, step,
				Dates.formatLocalDateTime(new Date()));
		Controllers.writeFileToResponse(response, file, fileName, MediaTypes.ZIP_CONTENT_TYPE);
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/startbackupexport", method = POST)
	public @ResponseBody JobView startBackupDataExportJob(@PathVariable("surveyId") int surveyId,
			@RequestBody BackupDataExportParameters parameters) throws IOException {
		User user = sessionManager.getLoggedUser();
		CollectSurvey survey = surveyManager.getById(surveyId);
		RecordFilter filter = createRecordFilter(survey, user, userGroupManager, null, parameters.onlyOwnedRecords, 
				parameters.modifiedSince, parameters.modifiedUntil);
		filter.setFilterExpression(parameters.filterExpression);
		filter.setKeyValues(parameters.keyAttributeValues);
		filter.setSummaryValues(parameters.summaryAttributeValues);
		if (parameters.countOnly) {
			RecordsCountJob job = jobManager.createJob(RecordsCountJob.class);
			job.setRecordFilter(filter);
			jobManager.start(job);
			return new JobView(job);
		} else {
			fullBackupJob = jobManager.createJob(SurveyBackupJob.class);
			fullBackupJob.setRecordFilter(filter);
			fullBackupJob.setSurvey(survey);
			fullBackupJob.setIncludeData(true);
			fullBackupJob.setIncludeRecordFiles(parameters.isIncludeRecordFiles());
			jobManager.start(fullBackupJob);
			return getFullBackupJobView();
		}		
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/exportresult.collect-data", method = GET)
	public void downloadBackupExportResult(HttpServletResponse response) throws FileNotFoundException, IOException {
		File file = fullBackupJob.getOutputFile();
		CollectSurvey survey = fullBackupJob.getSurvey();
		String surveyName = survey.getName();
		Controllers.writeFileToResponse(response, file,
				String.format("%s-%s.collect-data", surveyName, Dates.formatLocalDateTime(new Date())),
				MediaTypes.ZIP_CONTENT_TYPE);
	}

	@RequestMapping(value = "survey/{survey_id}/data/records/{record_id}/content/collect/data.collect-data", method = GET, produces = MediaTypes.ZIP_CONTENT_TYPE)
	public void exportRecordToCollectFormat(@PathVariable(value = "survey_id") int surveyId,
			@PathVariable(value = "record_id") int recordId, HttpServletResponse response)
			throws RecordPersistenceException, IOException {
		User user = sessionManager.getLoggedUser();
		CollectSurvey survey = surveyManager.getById(surveyId);

		RecordFilter filter = createRecordFilter(survey, user, userGroupManager);
		filter.setRecordId(recordId);

		// check that record exists
		List<CollectRecordSummary> summaries = recordManager.loadSummaries(filter);
		if (summaries.size() != 1) {
			throw new IllegalArgumentException(
					String.format("Could not find record with id %d or multiple records found", recordId));
		}

		CollectRecordSummary recordSummary = summaries.get(0);

		// start export job
		SurveyBackupJob job = jobManager.createJob(SurveyBackupJob.class);
		job.setRecordFilter(filter);
		job.setSurvey(survey);
		job.setIncludeData(true);
		job.setIncludeRecordFiles(true);
		jobManager.start(job, false);

		// write generated file to response
		File file = job.getOutputFile();
		String surveyName = survey.getName();
		String recordKeys = StringUtils.join(recordSummary.getRootEntityKeyValues(), '-');
		String outputFileName = String.format("%s-record-%s-%s.collect-data", surveyName, recordKeys,
				Dates.formatLocalDateTime(new Date()));
		Controllers.writeFileToResponse(response, file, outputFileName, MediaTypes.ZIP_CONTENT_TYPE);
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/stats", method = GET)
	public @ResponseBody RecordsStats generateStats(@PathVariable("surveyId") int surveyId) {
		Date[] period = recordManager.findWorkingPeriod(surveyId);
		if (period == null) {
			return RecordsStats.EMPTY;
		}
		RecordsStats stats = recordStatsGenerator.generate(surveyId, period);
		return stats;
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/validationreport", method = POST)
	public @ResponseBody JobProxy startValidationResportJob(@PathVariable("surveyId") int surveyId) {
		User user = sessionManager.getLoggedUser();
		Locale locale = sessionManager.getSessionState().getLocale();
		CollectSurvey survey = surveyManager.getById(surveyId);
		EntityDefinition rootEntityDef = survey.getSchema().getFirstRootEntityDefinition();
		ValidationReportJob job = jobManager.createJob(ValidationReportJob.class);
		Input input = new Input();
		input.setLocale(locale);
		input.setReportType(ReportType.CSV);
		RecordFilter recordFilter = createRecordFilter(survey, user, userGroupManager, rootEntityDef.getId(), false);
		input.setRecordFilter(recordFilter);
		job.setInput(input);
		this.validationReportJob = job;
		jobManager.start(job);
		return new JobProxy(job);
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/validationreport.csv", method = GET)
	public void downloadValidationReportResult(HttpServletResponse response) throws FileNotFoundException, IOException {
		File file = validationReportJob.getOutputFile();
		CollectSurvey survey = validationReportJob.getInput().getRecordFilter().getSurvey();
		String surveyName = survey.getName();
		Controllers.writeFileToResponse(response, file,
				String.format("collect-validation-report-%s-%s.csv", surveyName, Dates.formatDate(new Date())),
				MediaTypes.CSV_CONTENT_TYPE);
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/backupexportjob", method = GET)
	public @ResponseBody JobView getFullBackupJobView() {
		if (fullBackupJob == null) {
			return null;
		} else {
			JobView jobView = new JobView(fullBackupJob);
			jobView.putExtra("dataBackupErrors", fullBackupJob.getDataBackupErrors());
			return jobView;
		}
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/releaselock/{recordId}", method = POST)
	public @ResponseBody Response releaseRecordLock(@PathVariable int recordId) {
		CollectRecord activeRecord = sessionManager.getActiveRecord();
		Response res = new Response();
		if (activeRecord != null && activeRecord.getId() != null && activeRecord.getId().equals(recordId)) {
			recordManager.releaseLock(recordId);
			sessionManager.clearActiveRecord();
			appWS.sendMessage(new AppWS.RecordUnlockedMessage(recordId));
		} else {
			res.setErrorStatus();
			res.setErrorMessage(String.format("Cannot unlock record with id %d: it is not being edited by user %s",
					recordId, sessionManager.getLoggedUsername()));
		}
		return res;
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/randomgrid", method = POST, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody JobProxy startRandomRecordsGenerationJob(@PathVariable("surveyId") int surveyId,
			@RequestParam String oldMeasurement, @RequestParam String newMeasurement, @RequestParam Double percentage,
			@RequestParam String sourceGridSurveyFileName) {
		User user = sessionManager.getLoggedUser();
		RandomRecordsGenerationJob job = jobManager.createJob(RandomRecordsGenerationJob.class);
		CollectSurvey survey = surveyManager.getById(surveyId);
		job.setUser(user);
		job.setSurvey(survey);
		job.setOldMeasurement(oldMeasurement);
		job.setNewMeasurement(newMeasurement);
		job.setPercentage(percentage);
		job.setSourceGridSurveyFileName(sourceGridSurveyFileName);
		job.addStatusChangeListener(new WorkerStatusChangeListener() {
			public void statusChanged(WorkerStatusChangeEvent event) {
				if (event.getTo() == Worker.Status.COMPLETED) {
					// surveys list updated (temporary survey may have been created):
					// send surveys updated message to WS
					appWS.sendMessage(SURVEYS_UPDATED);
				}
			}
		});
		jobManager.startSurveyJob(job);
		return new JobProxy(job);
	}

	private Integer getStepNumberOrDefault(Integer stepNumber) {
		if (stepNumber == null) {
			stepNumber = Step.ENTRY.getStepNumber();
		}
		return stepNumber;
	}

	private RecordProxy toProxy(CollectRecord record) {
		String defaultLanguage = record.getSurvey().getDefaultLanguage();
		Locale locale = new Locale(defaultLanguage);
		ProxyContext context = new ProxyContext(locale, messageSource, surveyContext);
		return new RecordProxy(record, context);
	}

	private List<RecordSummaryProxy> toProxies(List<CollectRecordSummary> summaries) {
		List<RecordSummaryProxy> result = new ArrayList<RecordSummaryProxy>(summaries.size());
		for (CollectRecordSummary summary : summaries) {
			result.add(toSummaryProxy(summary));
		}
		return result;
	}

	private RecordSummaryProxy toSummaryProxy(CollectRecordSummary summary) {
		ProxyContext context = new ProxyContext(sessionManager.getSessionState().getLocale(), messageSource,
				surveyContext);
		return new RecordSummaryProxy(summary, context);
	}

	private void publishRecordPromotedEvents(CollectRecord record, String userName) {
		if (!eventQueue.isEnabled()) {
			return;
		}
		SessionState sessionState = sessionManager.getSessionState();
		EventProducerContext context = new EventProducer.EventProducerContext(messageSource, sessionState.getLocale(),
				userName);
		EventListenerToList consumer = new EventListenerToList();
		new EventProducer(context, consumer).produceFor(record);
		eventQueue.publish(new RecordTransaction(record.getSurvey().getName(), record.getId(),
				record.getStep().toRecordStep(), consumer.getList()));
	}

	private void publishRecordDeletedEvent(CollectRecord record, RecordStep recordStep, String userName) {
		if (!eventQueue.isEnabled()) {
			return;
		}
		List<RecordDeletedEvent> events = Arrays
				.asList(new RecordDeletedEvent(record.getSurvey().getName(), record.getId(), new Date(), userName));
		String surveyName = record.getSurvey().getName();
		eventQueue.publish(new RecordTransaction(surveyName, record.getId(), recordStep, events));
	}

	private User loadUser(Integer userId, String username) {
		if (userId != null) {
			return userManager.loadById(userId);
		} else if (username != null) {
			return userManager.loadByUserName(username);
		} else {
			return null;
		}
	}

	private boolean canDeleteRecords(int surveyId, Set<Integer> recordIds) {
		CollectSurvey survey = surveyManager.getById(surveyId);
		RecordFilter filter = new RecordFilter(survey);
		filter.setRecordIds(recordIds);
		List<CollectRecordSummary> recordSummaries = recordManager.loadSummaries(filter);
		User loggedUser = sessionManager.getLoggedUser();
		RecordAccessControlManager recordAccessControlManager = new RecordAccessControlManager();
		UserInGroup userInSurveyGroup = userGroupManager.findUserInGroupOrDescendants(survey.getUserGroupId(),
				loggedUser.getId());
		boolean canDeleteRecords = userInSurveyGroup != null && recordAccessControlManager.canDeleteRecords(loggedUser,
				userInSurveyGroup.getRole(), recordSummaries);
		return canDeleteRecords;
	}

	public static class SearchParameters {

		private int offset;
		private int maxNumberOfRows;

		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public int getMaxNumberOfRows() {
			return maxNumberOfRows;
		}

		public void setMaxNumberOfRows(int maxNumberOfRows) {
			this.maxNumberOfRows = maxNumberOfRows;
		}
	}

	public static class RecordSummarySearchParameters extends SearchParameters {

		private String username;
		private Integer userId;
		private String rootEntityName;
		private List<RecordSummarySortField> sortFields;
		private String[] keyValues;
		private boolean caseSensitiveKeyValues = false;
		private String[] qualifierValues;
		private String[] summaryValues;
		private Integer[] ownerIds;
		private boolean fullSummary = false;
		private boolean includeOwners = false;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public String getRootEntityName() {
			return rootEntityName;
		}

		public void setRootEntityName(String rootEntityName) {
			this.rootEntityName = rootEntityName;
		}

		public List<RecordSummarySortField> getSortFields() {
			return sortFields;
		}

		public void setSortFields(List<RecordSummarySortField> sortFields) {
			this.sortFields = sortFields;
		}

		public String[] getKeyValues() {
			return keyValues;
		}

		public void setKeyValues(String[] keyValues) {
			this.keyValues = keyValues;
		}

		public String[] getQualifierValues() {
			return qualifierValues;
		}

		public void setQualifierValues(String[] qualifierValues) {
			this.qualifierValues = qualifierValues;
		}

		public String[] getSummaryValues() {
			return summaryValues;
		}

		public void setSummaryValues(String[] summaryValues) {
			this.summaryValues = summaryValues;
		}

		public boolean isCaseSensitiveKeyValues() {
			return caseSensitiveKeyValues;
		}

		public void setCaseSensitiveKeyValues(boolean caseSensitiveKeyValues) {
			this.caseSensitiveKeyValues = caseSensitiveKeyValues;
		}

		public boolean isFullSummary() {
			return fullSummary;
		}

		public void setFullSummary(boolean fullSummary) {
			this.fullSummary = fullSummary;
		}

		public Integer[] getOwnerIds() {
			return ownerIds;
		}

		public void setOwnerIds(Integer[] ownerIds) {
			this.ownerIds = ownerIds;
		}

		public boolean isIncludeOwners() {
			return includeOwners;
		}

		public void setIncludeOwners(boolean includeOwners) {
			this.includeOwners = includeOwners;
		}
	}

	public static class RecordDeleteParameters {

		private int userId;
		private Integer[] recordIds;

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}

		public Integer[] getRecordIds() {
			return recordIds;
		}

		public void setRecordIds(Integer[] recordIds) {
			this.recordIds = recordIds;
		}
	}

	public static class BackupDataExportParameters {

		// records filter
		private boolean onlyOwnedRecords;
		private Date modifiedSince;
		private Date modifiedUntil;
		private String filterExpression;
		private List<String> keyAttributeValues = new ArrayList<String>();
		private List<String> summaryAttributeValues = new ArrayList<String>();
		private List<String> rootEntityKeyValues; // TODO check if they are used
		// export options
		private boolean countOnly;
		private boolean includeRecordFiles;

		public boolean isCountOnly() {
			return countOnly;
		}
		
		public void setCountOnly(boolean countOnly) {
			this.countOnly = countOnly;
		}
		
		public boolean isOnlyOwnedRecords() {
			return onlyOwnedRecords;
		}

		public void setOnlyOwnedRecords(boolean onlyOwnedRecords) {
			this.onlyOwnedRecords = onlyOwnedRecords;
		}

		public boolean isIncludeRecordFiles() {
			return includeRecordFiles;
		}

		public void setIncludeRecordFiles(boolean includeRecordFiles) {
			this.includeRecordFiles = includeRecordFiles;
		}

		public List<String> getRootEntityKeyValues() {
			return rootEntityKeyValues;
		}

		public void setRootEntityKeyValues(List<String> rootEntityKeyValues) {
			this.rootEntityKeyValues = rootEntityKeyValues;
		}

		public Date getModifiedSince() {
			return modifiedSince;
		}

		public void setModifiedSince(Date modifiedSince) {
			this.modifiedSince = modifiedSince;
		}

		public Date getModifiedUntil() {
			return modifiedUntil;
		}

		public void setModifiedUntil(Date modifiedUntil) {
			this.modifiedUntil = modifiedUntil;
		}

		public String getFilterExpression() {
			return filterExpression;
		}

		public void setFilterExpression(String filterExpression) {
			this.filterExpression = filterExpression;
		}

		public List<String> getKeyAttributeValues() {
			return keyAttributeValues;
		}

		public void setKeyAttributeValues(List<String> keyAttributeValues) {
			this.keyAttributeValues = keyAttributeValues;
		}

		public List<String> getSummaryAttributeValues() {
			return summaryAttributeValues;
		}

		public void setSummaryAttributeValues(List<String> summaryAttributeValues) {
			this.summaryAttributeValues = summaryAttributeValues;
		}
	}

	private static RecordFilter createRecordFilter(CollectSurvey survey, User user, UserGroupManager userGroupManager) {
		return createRecordFilter(survey, user, userGroupManager, null, false);
	}

	private static RecordFilter createRecordFilter(CollectSurvey survey, User user, UserGroupManager userGroupManager,
			Integer rootEntityId, boolean onlyOwnedRecords) {
		return createRecordFilter(survey, user, userGroupManager, rootEntityId, onlyOwnedRecords, null, null);
	}

	private static RecordFilter createRecordFilter(CollectSurvey survey, User user, UserGroupManager userGroupManager,
			Integer rootEntityId, boolean onlyOwnedRecords, Date modifiedSince, Date modifiedUntil) {
		if (rootEntityId == null) {
			rootEntityId = survey.getSchema().getFirstRootEntityDefinition().getId();
		}

		UserInGroup userInGroup = userGroupManager.findUserInGroupOrDescendants(survey.getUserGroupId(), user.getId());

		RecordFilter recordFilter = new RecordFilter(survey);
		recordFilter.setRootEntityId(rootEntityId);

		if (onlyOwnedRecords || user.getRole() == UserRole.ENTRY_LIMITED
				|| userInGroup != null && userInGroup.getRole() == UserGroupRole.DATA_CLEANER_LIMITED) {
			recordFilter.setOwnerId(user.getId());
		}
		if (user.getRole() != UserRole.ADMIN) {
			Map<String, String> qualifiers = userGroupManager.getQualifiers(survey.getUserGroupId(), user.getId());
			if (!qualifiers.isEmpty()) {
				recordFilter.setQualifiersByName(qualifiers);
			}
		}
		recordFilter.setModifiedSince(modifiedSince);
		recordFilter.setModifiedUntil(modifiedUntil);
		return recordFilter;
	}

	public static class CSVExportParametersForm extends CSVDataExportParametersBase {

		private Integer surveyId;
		private Integer rootEntityId;
		private Integer recordId;
		private Step stepGreaterOrEqual;
		private boolean exportOnlyOwnedRecords = false;
		private Date modifiedSince;
		private Date modifiedUntil;
		private String filterExpression;
		private List<String> keyAttributeValues = new ArrayList<String>();
		private List<String> summaryAttributeValues = new ArrayList<String>();

		public CSVDataExportParameters toExportParameters(CollectSurvey survey, User user,
				UserGroupManager userGroupManager) {
			CSVDataExportParameters result = new CSVDataExportParameters();
			RecordFilter recordFilter = createRecordFilter(survey, user, userGroupManager, rootEntityId,
					exportOnlyOwnedRecords, modifiedSince, modifiedUntil);
			recordFilter.setStepGreaterOrEqual(stepGreaterOrEqual);
			recordFilter.setKeyValues(keyAttributeValues);
			recordFilter.setSummaryValues(summaryAttributeValues);
			recordFilter.setFilterExpression(filterExpression);
			result.setRecordFilter(recordFilter);
			try {
				PropertyUtils.copyProperties(result, this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return result;
		}

		public Integer getSurveyId() {
			return surveyId;
		}

		public void setSurveyId(Integer surveyId) {
			this.surveyId = surveyId;
		}

		public Integer getRootEntityId() {
			return rootEntityId;
		}

		public void setRootEntityId(Integer rootEntityId) {
			this.rootEntityId = rootEntityId;
		}

		public Integer getRecordId() {
			return recordId;
		}

		public void setRecordId(Integer recordId) {
			this.recordId = recordId;
		}

		public Step getStepGreaterOrEqual() {
			return stepGreaterOrEqual;
		}

		public void setStepGreaterOrEqual(Step stepGreaterOrEqual) {
			this.stepGreaterOrEqual = stepGreaterOrEqual;
		}

		public boolean isExportOnlyOwnedRecords() {
			return exportOnlyOwnedRecords;
		}

		public void setExportOnlyOwnedRecords(boolean exportOnlyOwnedRecords) {
			this.exportOnlyOwnedRecords = exportOnlyOwnedRecords;
		}

		public Date getModifiedSince() {
			return modifiedSince;
		}

		public void setModifiedSince(Date modifiedSince) {
			this.modifiedSince = modifiedSince;
		}

		public Date getModifiedUntil() {
			return modifiedUntil;
		}

		public void setModifiedUntil(Date modifiedUntil) {
			this.modifiedUntil = modifiedUntil;
		}

		public String getFilterExpression() {
			return filterExpression;
		}

		public void setFilterExpression(String filterExpression) {
			this.filterExpression = filterExpression;
		}

		public List<String> getKeyAttributeValues() {
			return keyAttributeValues;
		}

		public void setKeyAttributeValues(List<String> keyAttributeValues) {
			this.keyAttributeValues = keyAttributeValues;
		}

		public List<String> getSummaryAttributeValues() {
			return summaryAttributeValues;
		}

		public void setSummaryAttributeValues(List<String> summaryAttributeValues) {
			this.summaryAttributeValues = summaryAttributeValues;
		}
	}
}