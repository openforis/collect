package org.openforis.collect.manager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordValidationReportItem;
import org.openforis.collect.model.RecordValidationReportGenerator;
import org.openforis.collect.model.User;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.collect.spring.SpringMessageSource;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * 
 * @author S. Ricci
 *
 */
public class ValidationReportProcess extends AbstractProcess<Void, ProcessStatus> {

	private static final String[] VALIDATION_REPORT_HEADERS = new String[] {"Record","Phase","Field","Error message"};

	private static Log LOG = LogFactory.getLog(ValidationReportProcess.class);
	
	private OutputStream outputStream;
	private RecordManager recordManager;
	private ReportType reportType;
	private CollectSurvey survey;
	private String rootEntityName;
	private User user;
	private String sessionId;
	private boolean includeConfirmedErrors;

	private ValidationMessageBuilder validationMessageBuilder;
	private CsvWriter csvWriter;
	
	public enum ReportType {
		CSV
	}

	public ValidationReportProcess(OutputStream outputStream,
			RecordManager recordManager,
			SpringMessageSource messageSource,
			ReportType reportType, 
			User user, String sessionId, 
			CollectSurvey survey, String rootEntityName, boolean includeConfirmedErrors) {
		super();
		this.outputStream = outputStream;
		this.recordManager = recordManager;
		this.reportType = reportType;
		this.user = user;
		this.sessionId = sessionId;
		this.survey = survey;
		this.rootEntityName = rootEntityName;
		this.includeConfirmedErrors = includeConfirmedErrors;
		validationMessageBuilder = ValidationMessageBuilder.createInstance(messageSource);
	}
	
	@Override
	protected void initStatus() {
		status = new ProcessStatus();
	}

	@Override
	public void startProcessing() throws Exception {
		super.startProcessing();
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName, (String) null);
		if ( summaries != null ) {
			status.setTotal(summaries.size());
			try {
				initWriter();
				writeHeader();
				for (CollectRecord summary : summaries) {
					//long start = System.currentTimeMillis();
					//print(outputStream, "Start validating record: " + recordKey);
					if ( status.isRunning() ) {
						Step step = summary.getStep();
						Integer recordId = summary.getId();
						try {
							final CollectRecord record = recordManager.checkout(survey, user, recordId, step, sessionId, true);
							writeValidationReport(record);
							status.incrementProcessed();
						} finally {
							recordManager.releaseLock(recordId);
						}
						//long elapsedMillis = System.currentTimeMillis() - start;
						//print(outputStream, "Validation of record " + recordKey + " completed in " + elapsedMillis + " millis");
					}
				}
				closeWriter();
				if ( status.isRunning() ) {
					status.complete();
				}
			} catch (IOException e) {
				status.error();
				String message = e.getMessage();
				status.setErrorMessage(message);
				if ( LOG.isErrorEnabled() ) {
					LOG.error(message, e);
				}
			}
		}
	}

	protected void initWriter() {
		switch (reportType) {
		case CSV:
			csvWriter = new CsvWriter(outputStream);
			break;
		}
	}
	
	private void closeWriter() throws IOException {
		switch (reportType) {
		case CSV:
			csvWriter.close();
			break;
		}
		
	}

	protected void writeValidationReport(CollectRecord record) throws IOException {
		
		recordManager.validate(record);
		RecordValidationReportGenerator reportGenerator = new RecordValidationReportGenerator(record);
		List<RecordValidationReportItem> validationItems = reportGenerator.generateValidationItems(
				validationMessageBuilder, ValidationResultFlag.ERROR, includeConfirmedErrors);
		for (RecordValidationReportItem item : validationItems) {
			writeValidationReportLine(record, item.getPath(), item.getMessage());
		}
	}
	
	protected void writeHeader() throws IOException {
		switch (reportType) {
		case CSV:
			csvWriter.writeHeaders(VALIDATION_REPORT_HEADERS);
		break;
		}
	}

	protected void writeValidationReportLine(CollectRecord record, String path, String message) {
		String recordKey = validationMessageBuilder.getRecordKey(record);
		String phase = record.getStep().name();
		String[] line = new String[]{recordKey, phase, path, message};
		switch (reportType) {
		case CSV:
			csvWriter.writeNext(line);
			break;
		}
	}

}
