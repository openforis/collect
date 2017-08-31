package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.io.File;
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.client.utils.URIBuilder;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.io.data.CSVDataExportJob;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RandomRecordGenerator;
import org.openforis.collect.manager.RandomRecordGenerator.Parameters;
import org.openforis.collect.manager.RecordAccessControlManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.Files;
import org.openforis.concurrency.JobManager;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SurveyContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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

	private static final String OLD_CLIENT_URL = "old_client.htm";

	// private static Log LOG = LogFactory.getLog(DataController.class);

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SurveyContext surveyContext;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private RandomRecordGenerator randomRecordGenerator;
	@Autowired
	private RecordSessionManager sessionManager;
	@Autowired
	private JobManager jobManager;
	
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
	
	@RequestMapping(value = "survey/{surveyId}/data/records/summary.json", method=GET)
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
		filter.setOffset(params.getOffset());
		filter.setMaxNumberOfRecords(params.getMaxNumberOfRows());
		
		//load summaries
		List<CollectRecord> summaries = recordManager.loadSummaries(filter, params.getSortFields());
		result.put("records", toProxies(summaries));
		
		//count total records
		int count = recordManager.countRecords(filter);
		result.put("count", count);
		
		return result;
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/{recordId}/edit.htm", method=GET)
	public ModelAndView editRecord(@PathVariable int surveyId, @PathVariable int recordId ) throws Exception {
		URIBuilder uriBuilder = new URIBuilder("redirect:/" + OLD_CLIENT_URL);
		uriBuilder.addParameter("edit", "true");
		uriBuilder.addParameter("surveyId", Integer.toString(surveyId));
		uriBuilder.addParameter("recordId", Integer.toString(recordId));
		String url = uriBuilder.toString();
		//String url = String.format("redirect:/index.htm?edit=true&surveyId=%d&recordId=%d", surveyId, recordId);
		return new ModelAndView(url);
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

	@Transactional
	@RequestMapping(value = "survey/{surveyId}/data/records", method=POST, consumes=APPLICATION_JSON_VALUE)
	public @ResponseBody
	RecordProxy newRecord(@PathVariable int surveyId, @RequestBody NewRecordParameters params) throws RecordPersistenceException {
		String sessionId = sessionManager.getSessionState().getSessionId();
		User user = sessionManager.getSessionState().getUser();
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		String rootEntityName = ObjectUtils.defaultIfNull(params.getRootEntityName(), survey.getSchema().getFirstRootEntityDefinition().getName());
		String versionName = ObjectUtils.defaultIfNull(params.getVersionName(), survey.getLatestVersion() != null ? survey.getLatestVersion().getName(): null);
		CollectRecord record = recordManager.create(survey, rootEntityName, user, versionName, sessionId);
		recordManager.save(record, user, sessionId);
		return toProxy(record);
	}
	
	@Transactional
	@RequestMapping(value = "survey/{surveyId}/data/records/random.json", method=POST, consumes=APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<RecordEvent> createRandomRecord(@PathVariable int surveyId, @RequestBody Parameters params) throws RecordPersistenceException {
		CollectRecord record = randomRecordGenerator.generate(surveyId, params);
		User user = userManager.loadById(params.getUserId());
		List<RecordEvent> events = new EventProducer().produceFor(record, user.getUsername());
		return events;
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
			RecordFilter recordFilter = new RecordFilter(survey);
			recordFilter.setRecordId(recordId);
			recordFilter.setStepGreaterOrEqual(Step.valueOf(stepNumber));
			recordFilter.setRootEntityId(survey.getSchema().getFirstRootEntityDefinition().getId());
			job.setRecordFilter(recordFilter);
			File outputFile = File.createTempFile("record_export", ".zip");
			job.setOutputFile(outputFile);
			job.setAlwaysGenerateZipFile(true);
			jobManager.start(job, false);
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
	
	private List<RecordProxy> toProxies(List<CollectRecord> summaries) {
		List<RecordProxy> result = new ArrayList<RecordProxy>(summaries.size());
		for (CollectRecord summary : summaries) {
			result.add(toProxy(summary));
		}
		return result;
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
	}
	
	public static class NewRecordParameters {
		
		private String rootEntityName;
		private String versionName;
		
		public String getRootEntityName() {
			return rootEntityName;
		}
		
		public void setRootEntityName(String rootEntityName) {
			this.rootEntityName = rootEntityName;
		}

		public String getVersionName() {
			return versionName;
		}

		public void setVersionName(String versionName) {
			this.versionName = versionName;
		}
	}
}