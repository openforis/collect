package org.openforis.collect.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.util.ValidationMessageHelper;
import org.openforis.collect.web.session.SessionState;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
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
	private ValidationMessageHelper validationMessageHelper;
	
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
	
	@RequestMapping(value = "/validationReport", method = RequestMethod.GET)
	public void validationReport(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(required=true) String s, @RequestParam(required=true) String r, @RequestParam(required=false, defaultValue="en_US") String locale) throws IOException {
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; fileName=validation_report.csv");
		ServletOutputStream outputStream = response.getOutputStream();
		try {
			if ( s == null || r == null || locale == null) {
				outputStream.println("Wrong parameters: please specify 's' (survey), 'r' (root entity name) and 'locale' string rappresentation of locale");
				return;
			}
			CollectSurvey survey = surveyManager.get(s);
			if ( survey == null ) {
				print(outputStream, "Survey not found");
				return;
			}
			SessionState sessionState = getSessionState(request);
			Locale l = LocaleUtils.toLocale(locale);
			User user = sessionState.getUser();
			String sessionId = sessionState.getSessionId();
			List<CollectRecord> summaries = recordManager.loadSummaries(survey, r, (String) null);
			if ( summaries != null ) {
				CsvWriter csvWriter = new CsvWriter(outputStream);
				writeHeader(csvWriter);
				for (CollectRecord summary : summaries) {
					//long start = System.currentTimeMillis();
					//print(outputStream, "Start validating record: " + recordKey);
					Integer id = summary.getId();
					Step step = summary.getStep();
					final CollectRecord record = recordManager.checkout(survey, user, id, step.getStepNumber(), sessionId, true);
					printValidationReport(csvWriter, record, l);
					recordManager.releaseLock(id);
					//long elapsedMillis = System.currentTimeMillis() - start;
					//print(outputStream, "Validation of record " + recordKey + " completed in " + elapsedMillis + " millis");
				}
			}
		} catch (Exception e) {
			outputStream.println("ERROR - Validation of records not completed: " + e.getMessage());
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	protected void printValidationReport(final CsvWriter writer, final CollectRecord record, final Locale locale) throws IOException {
		final ModelVersion version = record.getVersion();
		Entity rootEntity = record.getRootEntity();
		recordManager.addEmptyNodes(rootEntity);
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if ( node instanceof Attribute ) {
					Attribute<?,?> attribute = (Attribute<?, ?>) node;
					ValidationResults validationResults = attribute.validateValue();
					if ( validationResults.hasErrors() ) {
						writeValidationResults(writer, attribute, validationResults, locale);
					}
				} else if ( node instanceof Entity ) {
					Entity entity = (Entity) node;
					EntityDefinition definition = entity.getDefinition();
					List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
					for (NodeDefinition childDefinition : childDefinitions) {
						if ( version == null || version.isApplicable(childDefinition) ) {
							String childName = childDefinition.getName();
							ValidationResultFlag validateMaxCount = entity.validateMaxCount( childName );
							if ( validateMaxCount.isError() ) {
								writeMaxCountError(writer, entity, childName, locale);
							}
							ValidationResultFlag validateMinCount = entity.validateMinCount( childName );
							if ( validateMinCount.isError() ) {
								writeMinCountError(writer, entity, childName, locale);
							}
						}
					}
				}
			}
		});
		writer.close();
	}
	
	protected void writeHeader(CsvWriter writer) throws IOException {
		writer.writeHeaders(new String[] {"path","message"});
	}

	protected void writeValidationResults(CsvWriter writer, Attribute<?,?> attribute, ValidationResults validationResults, Locale locale) {
		List<ValidationResult> items = null;
		List<ValidationResult> errors = validationResults.getErrors();
		List<ValidationResult> warnings = validationResults.getWarnings();
		if ( errors != null && ! errors.isEmpty() ) {
			items = errors;
		} else if ( warnings != null )  {
			items = warnings;
		}
		if ( items != null ) {
			List<String> messages = new ArrayList<String>();
			for (ValidationResult validationResult : items) {
				String message = validationMessageHelper.getValidationMessage(attribute, validationResult, locale);
				if ( ! messages.contains(message) ) {
					messages.add(message);
				}
			}
			if ( ! messages.isEmpty() ) {
				CollectRecord record = (CollectRecord) attribute.getRecord();
				String recordKey = getRecordKey(record);
				String path = validationMessageHelper.getPrettyFormatPath(attribute, locale);
				for (String message : messages) {
					writeValidationReportLine(writer, recordKey, path, message);
				}
			}
		}
	}
	
	private void writeMinCountError(CsvWriter writer, Entity parentEntity, String childName, Locale locale) {
		writeCountError(true, writer, parentEntity, childName, locale);
	}

	private void writeMaxCountError(CsvWriter writer, Entity parentEntity, String childName, Locale locale) {
		writeCountError(false, writer, parentEntity, childName, locale);
	}
	
	protected void writeCountError(boolean min, CsvWriter writer, Entity parentEntity, String childName, Locale locale) {
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		NodeDefinition childDefn = parentEntityDefn.getChildDefinition(childName);
		String message = min ? validationMessageHelper.getMinCountValidationMessage(childDefn) : validationMessageHelper.getMaxCountValidationMessage(childDefn);
		String path = validationMessageHelper.getPrettyFormatPath(parentEntity, childName, locale);
		CollectRecord record = (CollectRecord) parentEntity.getRecord();
		String recordKey = getRecordKey(record);
		writeValidationReportLine(writer, recordKey, path, message);
	}

	protected void writeValidationReportLine(CsvWriter writer, String recordKey, String path, String message) {
		String[] line = new String[]{recordKey, path, message};
		writer.writeNext(line);
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
