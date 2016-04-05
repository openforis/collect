package org.openforis.collect.web.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.ValidationReportProcess;
import org.openforis.collect.manager.ValidationReportProcess.ReportType;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.User;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.collect.spring.SpringMessageSource;
import org.openforis.collect.utils.Dates;
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
	@Autowired
	private SpringMessageSource messageContextHolder;
	
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
				ValidationMessageBuilder validationMessageHelper = ValidationMessageBuilder.createInstance(messageContextHolder);
				print(outputStream, "Records to validate: " + summaries.size());
				for (CollectRecord summary : summaries) {
					String recordKey = validationMessageHelper.getRecordKey(summary);
					long start = System.currentTimeMillis();
					print(outputStream, "Start validating record: " + recordKey);
					Integer id = summary.getId();
					Step step = summary.getStep();
					recordManager.validateAndSave(survey, user, sessionId, id, step);
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
	
	@RequestMapping(value = "/validationReport", method = RequestMethod.GET)
	public void validationReport(HttpServletRequest request, HttpServletResponse response
			, @RequestParam(required=false) String surveyName
			, @RequestParam(required=false) Integer rootEntityId
			, @RequestParam(required=false, defaultValue="en_US") String locale
			, String[] recordKeys
			, @RequestParam(required=false) Date modifiedSince
			) throws IOException {
		ServletOutputStream outputStream = response.getOutputStream();
		try {
			if ( surveyName == null || rootEntityId == null || locale == null) {
				outputStream.println("Wrong parameters: please specify 'surveyName' (name of the survey), 'rootEntityId' (root entity id) and 'locale' string rappresentation of locale");
				return;
			}
			CollectSurvey survey = surveyManager.get(surveyName);
			if ( survey == null ) {
				print(outputStream, "Survey not found");
				return;
			}
			response.setContentType("text/csv");
			String outputFileName = String.format("%s_validation_report_%s.csv", surveyName, Dates.formatDateTime(new Date()));
			response.setHeader("Content-Disposition", "attachment; fileName=" + outputFileName);
			SessionState sessionState = getSessionState(request);
			User user = sessionState.getUser();
			String sessionId = sessionState.getSessionId();
			RecordFilter recordFilter = new RecordFilter(survey, rootEntityId);
			recordFilter.setKeyValues(recordKeys);
			recordFilter.setModifiedSince(modifiedSince);
			ValidationReportProcess process = new ValidationReportProcess(outputStream, recordManager, messageContextHolder, 
					ReportType.CSV, user, sessionId, recordFilter, true, LocaleUtils.toLocale(locale));
			process.init();
			process.call();
		} catch (Exception e) {
			//outputStream.println("ERROR - Validation of records not completed: " + e.getMessage());
			LOG.error("ERROR - Validation of records not completed: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private void print(ServletOutputStream outputStream, String message) throws IOException {
		outputStream.println(message);
		outputStream.flush();
		LOG.info(message);
	}

}
