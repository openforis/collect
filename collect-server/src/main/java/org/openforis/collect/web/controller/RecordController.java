package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.io.data.CSVDataExportJob;
import org.openforis.collect.io.data.DataImportSummary;
import org.openforis.collect.io.data.DataRestoreJob;
import org.openforis.collect.io.data.DataRestoreSummaryJob;
import org.openforis.collect.io.data.csv.CSVDataExportParameters;
import org.openforis.collect.io.data.csv.CSVDataExportParameters.HeadingSource;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RandomRecordGenerator;
import org.openforis.collect.manager.RecordAccessControlManager;
import org.openforis.collect.manager.RecordGenerator;
import org.openforis.collect.manager.RecordGenerator.NewRecordParameters;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.model.proxy.RecordSummaryProxy;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.remoting.service.dataimport.DataImportSummaryProxy;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.Files;
import org.openforis.collect.web.controller.CollectJobController.JobView;
import org.openforis.collect.web.controller.RecordStatsGenerator.RecordsStats;
import org.openforis.collect.web.session.SessionState;
import org.openforis.commons.web.HttpResponses;
import org.openforis.commons.web.Response;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SurveyContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
@Scope(SCOPE_SESSION)
@RequestMapping("api")
public class RecordController extends BasicController implements Serializable {

	private static final long serialVersionUID = 1L;

	// private static Log LOG = LogFactory.getLog(DataController.class);

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SurveyContext surveyContext;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private RandomRecordGenerator randomRecordGenerator;
	@Autowired
	private RecordGenerator recordGenerator;
	@Autowired
	private RecordSessionManager sessionManager;
	@Autowired
	private CollectJobManager jobManager;
	@Autowired
	private RecordStatsGenerator recordStatsGenerator;

	private CSVDataExportJob csvDataExportJob;
	private DataRestoreSummaryJob dataRestoreSummaryJob;
	
	@RequestMapping(value = "survey/{surveyId}/data/records/{recordId}/binary_data.json", method=GET)
	public @ResponseBody
	Map<String, Object> loadData(
			@PathVariable int surveyId,
			@PathVariable int recordId,
			@RequestParam(value="step") Integer stepNumber) throws Exception {
		stepNumber = getStepNumberOrDefault(stepNumber);
		CollectSurvey survey = surveyManager.getById(surveyId);
		byte[] data = recordManager.loadBinaryData(survey, recordId, Step.valueOf(stepNumber));
		byte[] encoded = Base64.encodeBase64(data);
		String result = new String(encoded);
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("data", result);
		
 		return map;
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/count.json", method=GET)
	public @ResponseBody
	int getCount(@PathVariable int surveyId,
			@RequestParam(value="rootEntityDefinitionId", required=false) Integer rootEntityDefinitionId,
			@RequestParam(value="step", required=false) Integer stepNumber) throws Exception {
		CollectSurvey survey = surveyManager.getById(surveyId);
		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(rootEntityDefinitionId);
		if (stepNumber != null) {
			filter.setStepGreaterOrEqual(Step.valueOf(stepNumber));
		}
		int count = recordManager.countRecords(filter);
		return count;
	}
	
	@RequestMapping(value = "survey/{surveyId}/data/records/summary", method=GET)
	public @ResponseBody Map<String, Object> loadRecordSummaries(
			@PathVariable int surveyId,
			@Valid RecordSummarySearchParameters params) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		CollectSurvey survey = surveyManager.getById(surveyId);
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDefinition = params.getRootEntityName() == null ? schema.getFirstRootEntityDefinition() : 
			schema.getRootEntityDefinition(params.getRootEntityName());
		
		RecordFilter filter = new RecordFilter(survey, rootEntityDefinition.getId());
		filter.setKeyValues(params.getKeyValues());
		filter.setCaseSensitiveKeyValues(params.isCaseSensitiveKeyValues());
		filter.setOffset(params.getOffset());
		filter.setMaxNumberOfRecords(params.getMaxNumberOfRows());
		
		//load summaries
		List<CollectRecordSummary> summaries = recordManager.loadFullSummaries(filter, params.getSortFields());
		result.put("records", toProxies(summaries));
		
		//count total records
		int count = recordManager.countRecords(filter);
		result.put("count", count);
		
		return result;
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/{recordId}", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	RecordProxy loadRecord(
			@PathVariable int surveyId, 
			@PathVariable int recordId,
			@RequestParam(value="step", required=false) Integer stepNumber) throws RecordPersistenceException {
		stepNumber = getStepNumberOrDefault(stepNumber);
		CollectSurvey survey = surveyManager.getById(surveyId);
		CollectRecord record = recordManager.load(survey, recordId, Step.valueOf(stepNumber));
		return toProxy(record);
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/{recordId}", method=PATCH, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody Response updateOwner(@PathVariable int surveyId, @PathVariable int recordId,
			@RequestBody Map<String, String> body) throws RecordLockedException, MultipleEditException {
		String ownerIdStr = body.get("ownerId");
		Integer ownerId = ownerIdStr == null ? null : Integer.parseInt(ownerIdStr);
		CollectSurvey survey = surveyManager.getById(surveyId);
		SessionState sessionState = sessionManager.getSessionState();
		recordManager.assignOwner(survey, recordId, ownerId, sessionState.getUser(), sessionState.getSessionId());
		return new Response();
	}

	@Transactional
	@RequestMapping(value = "survey/{surveyId}/data/records", method=POST, consumes=APPLICATION_JSON_VALUE)
	public @ResponseBody
	RecordProxy newRecord(@PathVariable int surveyId, @RequestBody NewRecordParameters params) throws RecordPersistenceException {
		User user = sessionManager.getSessionState().getUser();
		CollectSurvey survey = surveyManager.getById(surveyId);
		params.setRootEntityName(ObjectUtils.defaultIfNull(params.getRootEntityName(), survey.getSchema().getFirstRootEntityDefinition().getName()));
		params.setVersionName(ObjectUtils.defaultIfNull(params.getVersionName(), survey.getLatestVersion() != null ? survey.getLatestVersion().getName(): null));
		params.setUserId(user.getId());
		CollectRecord record = recordGenerator.generate(surveyId, params, params.getRecordKey());
		return toProxy(record);
	}
	
	@RequestMapping(value = "survey/{surveyId}/data/import/records/summary", method=POST, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody
	JobView generateRecordImportSummary(@PathVariable int surveyId, @RequestParam("file") MultipartFile multipartFile, 
			@RequestParam String rootEntityName) throws IOException {
		File file = File.createTempFile("ofc_data_restore", ".collect-data");
		FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
		CollectSurvey survey = surveyManager.getById(surveyId);
		DataRestoreSummaryJob job = jobManager.createJob(DataRestoreSummaryJob.class);
		job.setFullSummary(true);
		job.setFile(file);
		job.setPublishedSurvey(survey);
		job.setCloseRecordProviderOnComplete(false);
		jobManager.start(job);
		this.dataRestoreSummaryJob = job;
		return new JobView(job);
	}

	@RequestMapping(value = "survey/{surveyId}/data/import/records/summary", method=GET)
	public @ResponseBody
	DataImportSummaryProxy getRecordImportSummary(@PathVariable int surveyId) throws IOException {
		if (this.dataRestoreSummaryJob == null || ! this.dataRestoreSummaryJob.isCompleted()) {
			throw new IllegalStateException("Data restore summary not generated or an error occurred during the generation");
		}
		DataImportSummary summary = this.dataRestoreSummaryJob.getSummary();
		return new DataImportSummaryProxy(summary, sessionManager.getSessionState().getLocale());
	}
	
	@RequestMapping(value = "survey/{surveyId}/data/import/records", method=POST)
	public @ResponseBody
	JobView startRecordImport(@PathVariable int surveyId, @RequestParam List<Integer> entryIdsToImport, 
			@RequestParam(defaultValue="true") boolean validateRecords) throws IOException {
		DataRestoreJob job = jobManager.createJob(DataRestoreJob.class);
		job.setFile(dataRestoreSummaryJob.getFile());
		job.setValidateRecords(validateRecords);
		job.setRecordProvider(dataRestoreSummaryJob.getRecordProvider());
		job.setPackagedSurvey(dataRestoreSummaryJob.getPackagedSurvey());
		job.setPublishedSurvey(dataRestoreSummaryJob.getPublishedSurvey());
		job.setEntryIdsToImport(entryIdsToImport);
		job.setRecordFilesToBeDeleted(dataRestoreSummaryJob.getSummary().getConflictingRecordFiles(entryIdsToImport));
		job.setRestoreUploadedFiles(true);
		job.setValidateRecords(validateRecords);
		jobManager.start(job);
		return new JobView(job);
	}
	
	@Transactional
	@RequestMapping(value = "survey/{surveyId}/data/records/random", method=POST, consumes=APPLICATION_JSON_VALUE)
	public @ResponseBody
	RecordProxy createRandomRecord(@PathVariable int surveyId, @RequestBody NewRecordParameters params) throws RecordPersistenceException {
		CollectRecord record = randomRecordGenerator.generate(surveyId, params);
		return toProxy(record);
	}

	@RequestMapping(value = "survey/{survey_id}/data/records/{record_id}/steps/{step}/csv_content.zip", method=GET, produces=Files.ZIP_CONTENT_TYPE)
	public void exportRecord(
			@PathVariable(value="survey_id") int surveyId, 
			@PathVariable(value="record_id") int recordId,
			@PathVariable(value="step") int stepNumber,
			HttpServletResponse response
			) throws RecordPersistenceException, IOException {
		CollectSurvey survey = surveyManager.getById(surveyId);
		CollectRecord record = recordManager.load(survey, recordId);
		RecordAccessControlManager accessControlManager = new RecordAccessControlManager();
		if (accessControlManager.canEdit(sessionManager.getSessionState().getUser(), record)) {
			CSVDataExportJob job = jobManager.createJob(CSVDataExportJob.class);
			CSVDataExportParameters parameters = new CSVDataExportParameters();
			RecordFilter recordFilter = new RecordFilter(survey);
			recordFilter.setRecordId(recordId);
			recordFilter.setStepGreaterOrEqual(Step.valueOf(stepNumber));
			recordFilter.setRootEntityId(survey.getSchema().getFirstRootEntityDefinition().getId());
			parameters.setRecordFilter(recordFilter);
			parameters.setAlwaysGenerateZipFile(true);
			job.setParameters(parameters);
			File outputFile = File.createTempFile("record_export", ".zip");
			job.setOutputFile(outputFile);
			jobManager.startSurveyJob(job);
			if (job.isCompleted()) {
				String fileName = String.format("record_data_%s.zip", Dates.formatDate(new Date()));
				Controllers.writeFileToResponse(response, outputFile, fileName, Files.ZIP_CONTENT_TYPE);
			}
		}
	}
	
	@RequestMapping(value="data/records/{recordId}/surveyId", method=GET)
	public @ResponseBody int loadSurveyId(@PathVariable int recordId) {
		return recordManager.loadSurveyId(recordId);
	}
	
	@RequestMapping(value="survey/{surveyId}/data/records/startcsvexport", method=POST)
	public @ResponseBody JobView startDataExportJob(
			@PathVariable Integer surveyId,
			@RequestBody CSVExportParametersForm parameters) throws IOException {
		User user = sessionManager.getSessionState().getUser();
		CollectSurvey survey = surveyManager.getById(surveyId);
		
		csvDataExportJob = jobManager.createJob(CSVDataExportJob.class);
		csvDataExportJob.setSurvey(survey);

		csvDataExportJob.setOutputFile(File.createTempFile("collect-csv-data-export", ".zip"));
		
		CSVDataExportParameters exportParameters = parameters.toExportParameters(survey, user);
		csvDataExportJob.setParameters(exportParameters);
		
		jobManager.start(csvDataExportJob);
		
		return new JobView(csvDataExportJob);
	}
	
	@RequestMapping(value="survey/{surveyId}/data/records/currentcsvexport", method=GET)
	public @ResponseBody JobView getCsvDataExportJob(HttpServletResponse response) {
		if (csvDataExportJob == null) {
			HttpResponses.setNoContentStatus(response);
			return null;
		} else {
			return new JobView(csvDataExportJob);
		}
	}
	
	@RequestMapping(value="survey/{surveyId}/data/records/csvexportresult.zip", method=GET)
	public void downloadResult(HttpServletResponse response) throws FileNotFoundException, IOException {
		File file = csvDataExportJob.getOutputFile();
		RecordFilter recordFilter = csvDataExportJob.getParameters().getRecordFilter();
		CollectSurvey survey = recordFilter.getSurvey();
		String surveyName = survey.getName();
		Controllers.writeFileToResponse(response, file, 
				String.format("collect-data-export-%s-%s.zip", surveyName, Dates.formatDate(new Date())), 
				Controllers.ZIP_CONTENT_TYPE);
	}
	
	@RequestMapping(value="survey/{surveyId}/data/records/stats", method=GET)
	public @ResponseBody RecordsStats generateStats(@PathVariable Integer surveyId) {
		Date[] period = recordManager.findWorkingPeriod(surveyId);
		if (period == null) {
			return RecordsStats.EMPTY;
		}
		RecordsStats stats = recordStatsGenerator.generate(surveyId, period);
		return stats;
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
		ProxyContext context = new ProxyContext(sessionManager.getSessionState().getLocale(), messageSource, surveyContext);
		return new RecordSummaryProxy(summary, context);
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
		
		private String rootEntityName;
		private List<RecordSummarySortField> sortFields;
		private String[] keyValues;
		private boolean caseSensitiveKeyValues = false;

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
		
		public boolean isCaseSensitiveKeyValues() {
			return caseSensitiveKeyValues;
		}
		
		public void setCaseSensitiveKeyValues(boolean caseSensitiveKeyValues) {
			this.caseSensitiveKeyValues = caseSensitiveKeyValues;
		}
	}
	
	public static class CSVExportParametersForm {
		
		private Integer surveyId;
		private Integer rootEntityId;
		private Integer entityId;
		private Integer recordId;
		private Step stepGreaterOrEqual;
		private boolean exportOnlyOwnedRecords = false;
		private boolean alwaysGenerateZipFile = false;
		private String multipleAttributeValueSeparator = ", ";
		private String fieldHeadingSeparator = "_";
		private boolean includeAllAncestorAttributes = false;
		private boolean includeCodeItemPositionColumn = false;
		private boolean includeKMLColumnForCoordinates = false;
		private boolean includeEnumeratedEntities = true;
		private boolean includeCompositeAttributeMergedColumn = false;
		private boolean includeCodeItemLabelColumn = false;
		private boolean includeGroupingLabels = true;
		private boolean codeAttributeExpanded = false;
		private int maxMultipleAttributeValues = 10;
		private int maxExpandedCodeAttributeItems = 30;
		private HeadingSource headingSource = HeadingSource.ATTRIBUTE_NAME;
		private String languageCode = null;
		
		public CSVDataExportParameters toExportParameters(CollectSurvey survey, User user) {
			CSVDataExportParameters result = new CSVDataExportParameters();
			RecordFilter recordFilter = new RecordFilter(survey);
			recordFilter.setRootEntityId(rootEntityId);
			result.setRecordFilter(recordFilter);
			if (exportOnlyOwnedRecords) {
				recordFilter.setOwnerId(user.getId());
			}
			try {
				BeanUtils.copyProperties(result, this);
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

		public Integer getEntityId() {
			return entityId;
		}

		public void setEntityId(Integer entityId) {
			this.entityId = entityId;
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
		
		public boolean isAlwaysGenerateZipFile() {
			return alwaysGenerateZipFile;
		}

		public void setAlwaysGenerateZipFile(boolean alwaysGenerateZipFile) {
			this.alwaysGenerateZipFile = alwaysGenerateZipFile;
		}

		public String getMultipleAttributeValueSeparator() {
			return multipleAttributeValueSeparator;
		}

		public void setMultipleAttributeValueSeparator(String multipleAttributeValueSeparator) {
			this.multipleAttributeValueSeparator = multipleAttributeValueSeparator;
		}

		public String getFieldHeadingSeparator() {
			return fieldHeadingSeparator;
		}

		public void setFieldHeadingSeparator(String fieldHeadingSeparator) {
			this.fieldHeadingSeparator = fieldHeadingSeparator;
		}

		public boolean isIncludeAllAncestorAttributes() {
			return includeAllAncestorAttributes;
		}

		public void setIncludeAllAncestorAttributes(boolean includeAllAncestorAttributes) {
			this.includeAllAncestorAttributes = includeAllAncestorAttributes;
		}

		public boolean isIncludeCodeItemPositionColumn() {
			return includeCodeItemPositionColumn;
		}

		public void setIncludeCodeItemPositionColumn(boolean includeCodeItemPositionColumn) {
			this.includeCodeItemPositionColumn = includeCodeItemPositionColumn;
		}

		public boolean isIncludeKMLColumnForCoordinates() {
			return includeKMLColumnForCoordinates;
		}

		public void setIncludeKMLColumnForCoordinates(boolean includeKMLColumnForCoordinates) {
			this.includeKMLColumnForCoordinates = includeKMLColumnForCoordinates;
		}

		public boolean isIncludeEnumeratedEntities() {
			return includeEnumeratedEntities;
		}

		public void setIncludeEnumeratedEntities(boolean includeEnumeratedEntities) {
			this.includeEnumeratedEntities = includeEnumeratedEntities;
		}

		public boolean isIncludeCompositeAttributeMergedColumn() {
			return includeCompositeAttributeMergedColumn;
		}

		public void setIncludeCompositeAttributeMergedColumn(boolean includeCompositeAttributeMergedColumn) {
			this.includeCompositeAttributeMergedColumn = includeCompositeAttributeMergedColumn;
		}

		public boolean isIncludeCodeItemLabelColumn() {
			return includeCodeItemLabelColumn;
		}

		public void setIncludeCodeItemLabelColumn(boolean includeCodeItemLabelColumn) {
			this.includeCodeItemLabelColumn = includeCodeItemLabelColumn;
		}

		public boolean isIncludeGroupingLabels() {
			return includeGroupingLabels;
		}

		public void setIncludeGroupingLabels(boolean includeGroupingLabels) {
			this.includeGroupingLabels = includeGroupingLabels;
		}

		public boolean isCodeAttributeExpanded() {
			return codeAttributeExpanded;
		}

		public void setCodeAttributeExpanded(boolean codeAttributeExpanded) {
			this.codeAttributeExpanded = codeAttributeExpanded;
		}

		public int getMaxMultipleAttributeValues() {
			return maxMultipleAttributeValues;
		}

		public void setMaxMultipleAttributeValues(int maxMultipleAttributeValues) {
			this.maxMultipleAttributeValues = maxMultipleAttributeValues;
		}

		public int getMaxExpandedCodeAttributeItems() {
			return maxExpandedCodeAttributeItems;
		}

		public void setMaxExpandedCodeAttributeItems(int maxExpandedCodeAttributeItems) {
			this.maxExpandedCodeAttributeItems = maxExpandedCodeAttributeItems;
		}

		public HeadingSource getHeadingSource() {
			return headingSource;
		}

		public void setHeadingSource(HeadingSource headingSource) {
			this.headingSource = headingSource;
		}

		public String getLanguageCode() {
			return languageCode;
		}

		public void setLanguageCode(String languageCode) {
			this.languageCode = languageCode;
		}
	}
	
	
}