package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.openforis.collect.io.data.CSVDataExportJob;
import org.openforis.collect.manager.RecordAccessControlManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.Files;
import org.openforis.collect.web.session.SessionState;
import org.openforis.concurrency.JobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author S. Ricci
 * 
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class RecordController extends BasicController implements Serializable {

	private static final long serialVersionUID = 1L;

	// private static Log LOG = LogFactory.getLog(DataController.class);

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordSessionManager sessionManager;
	@Autowired
	private JobManager jobManager;
	
	@RequestMapping(value = "/surveys/{survey_id}/records/{record_id}/steps/{step}/binary_data.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	Map<String, Object> loadData(@PathVariable(value="survey_id") int surveyId,
			@PathVariable(value="record_id") int recordId,
			@PathVariable(value="step") int stepNumber) throws Exception {
		CollectSurvey survey = surveyManager.getById(surveyId);
		byte[] data = recordManager.loadBinaryData(survey, recordId, Step.valueOf(stepNumber));
		byte[] encoded = Base64.encodeBase64(data);
		String result = new String(encoded);
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("data", result);
		
 		return map;
	}

	@RequestMapping(value = "/surveys/{survey_id}/records/count.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	int getCount(@PathVariable(value="survey_id") int surveyId,
			@RequestParam(value="rootEntityDefinitionId") int rootEntityDefinitionId,
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

	@RequestMapping(value = "/surveys/{survey_id}/records/{record_id}/edit.htm", method=GET)
	public ModelAndView editRecord(@PathVariable(value="survey_id") int surveyId,
			@PathVariable(value="record_id") int recordId ) throws Exception {
		URIBuilder uriBuilder = new URIBuilder("redirect:/index.htm");
		uriBuilder.addParameter("edit", "true");
		uriBuilder.addParameter("surveyId", Integer.toString(surveyId));
		uriBuilder.addParameter("recordId", Integer.toString(recordId));
		String url = uriBuilder.toString();
		//String url = String.format("redirect:/index.htm?edit=true&surveyId=%d&recordId=%d", surveyId, recordId);
		return new ModelAndView(url);
	}
	
	@RequestMapping(value = "/surveys/{survey_id}/records/{record_id}/steps/{step}/content.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	RecordProxy loadRecord(
			@PathVariable(value="survey_id") int surveyId, 
			@PathVariable(value="record_id") int recordId,
			@PathVariable(value="step") int stepNumber) throws RecordPersistenceException {
		CollectSurvey survey = surveyManager.getById(surveyId);
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = recordManager.checkout(survey, sessionState.getUser(), recordId, Step.valueOf(stepNumber), sessionState.getSessionId(), true);
		return new RecordProxy(record, sessionState.getLocale());
	}
	
	@RequestMapping(value = "/surveys/{survey_id}/records/{record_id}/steps/{step}/csv_content.zip", method=GET, produces=Files.ZIP_CONTENT_TYPE)
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
				writeFileToResponse(outputFile, Files.ZIP_CONTENT_TYPE, Long.valueOf(outputFile.length()).intValue(), 
						response, fileName);
			}
		}
	}
}
