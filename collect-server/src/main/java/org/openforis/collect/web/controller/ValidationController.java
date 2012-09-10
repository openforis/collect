package org.openforis.collect.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author S. Ricci
 * 
 * Controller that manages the validation of records
 *
 */
@Controller
public class ValidationController extends BasicController {
	private static Log LOG = LogFactory.getLog(ValidationController.class);
	
	@Autowired
	private RecordManager recordManager;
	
	@Autowired
	private SurveyManager surveyManager;
	
	@RequestMapping(value = "/validateAllRecords.htm", method = RequestMethod.GET)
	public void validateAllRecords(HttpServletRequest request, HttpServletResponse response, @RequestParam String s, @RequestParam String r) throws IOException {
		ServletOutputStream outputStream = response.getOutputStream();
		try {
			if ( s == null || r == null) {
				outputStream.println("Wrong parameters: please specify 's' (survey) and 'r' (root entity name).");
				return;
			}
			SessionState sessionState = getSessionState(request);
			User user = sessionState.getUser();
			String sessionId = sessionState.getSessionId();
			print(outputStream, "Starting validation of all records: ");
			CollectSurvey survey = surveyManager.get(s);
			if ( survey == null ) {
				print(outputStream, "Survey not found");
				return;
			}
			List<CollectRecord> summaries = recordManager.loadSummaries(survey, r, (String) null);
			if ( summaries != null ) {
				print(outputStream, "Records to validate: " + summaries.size());
				for (CollectRecord summary : summaries) {
					String recordKey = getRecordKey(summary);
					long start = System.currentTimeMillis();
					print(outputStream, "Start validating record: " + recordKey);
					Integer id = summary.getId();
					Step step = summary.getStep();
					recordManager.validate(survey, user, sessionId, id, step);
					long elapsedMillis = System.currentTimeMillis() - start;
					print(outputStream, "Validation of record " + recordKey + " completed in " + elapsedMillis + " millis");
				}
			}
			print(outputStream, "End of validation of all records.");
		} catch (Exception e) {
			outputStream.println("ERROR - Validation of records not completed: " + e.getMessage());
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	private void print(ServletOutputStream outputStream, String message) throws IOException {
		outputStream.println(message);
		outputStream.flush();
		LOG.info(message);
	}

	private String getRecordKey(CollectRecord record) {
		List<String> rootEntityKeyValues = record.getRootEntityKeyValues();
		List<String> cleanedKeys = new ArrayList<String>();
		for (String key : rootEntityKeyValues) {
			if ( StringUtils.isNotBlank(key) ) {
				cleanedKeys.add(key);
			}
		}
		String result = StringUtils.join(cleanedKeys, "-");
		return result;
	}
	
}
