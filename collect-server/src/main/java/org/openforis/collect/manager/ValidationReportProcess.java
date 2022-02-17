package org.openforis.collect.manager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordValidationReportGenerator;
import org.openforis.collect.model.RecordValidationReportItem;
import org.openforis.collect.model.User;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.path.Path;

/**
 * 
 * @author S. Ricci
 *
 */
public class ValidationReportProcess extends AbstractProcess<Void, ProcessStatus> {

	private static final String[] VALIDATION_REPORT_HEADERS = new String[] {
			"Record","Phase","Attribute Schema Path",
			"Field path","Field path (labels)","Error message"};

	private static final Logger LOG = LogManager.getLogger(ValidationReportProcess.class);
	
	private OutputStream outputStream;
	private RecordManager recordManager;
	private ReportType reportType;
	private RecordFilter recordFilter;
	private boolean includeConfirmedErrors;
	private Locale locale;

	private ValidationMessageBuilder validationMessageBuilder;
	private CsvWriter csvWriter;

	public enum ReportType {
		CSV
	}

	public ValidationReportProcess(OutputStream outputStream,
			RecordManager recordManager,
			MessageSource messageSource,
			ReportType reportType, 
			User user, String sessionId, 
			RecordFilter recordFilter, boolean includeConfirmedErrors) {
		this(outputStream, recordManager, messageSource, reportType, user, sessionId, recordFilter, includeConfirmedErrors, Locale.ENGLISH);
	}
	
	public ValidationReportProcess(OutputStream outputStream,
			RecordManager recordManager,
			MessageSource messageSource,
			ReportType reportType, 
			User user, String sessionId, 
			RecordFilter recordFilter, boolean includeConfirmedErrors, Locale locale) {
		super();
		this.outputStream = outputStream;
		this.recordManager = recordManager;
		this.reportType = reportType;
		this.recordFilter = recordFilter;
		this.includeConfirmedErrors = includeConfirmedErrors;
		this.validationMessageBuilder = ValidationMessageBuilder.createInstance(messageSource);
		this.locale = locale;
	}
	
	@Override
	protected void initStatus() {
		status = new ProcessStatus();
	}

	@Override
	public void startProcessing() throws Exception {
		super.startProcessing();
		List<CollectRecordSummary> summaries = recordManager.loadSummaries(recordFilter);
		if ( summaries != null ) {
			status.setTotal(summaries.size());
			try {
				initWriter();
				writeHeader();
				for (CollectRecordSummary summary : summaries) {
					//long start = System.currentTimeMillis();
					//print(outputStream, "Start validating record: " + recordKey);
					if ( status.isRunning() ) {
						Step step = summary.getStep();
						Integer recordId = summary.getId();
						CollectRecord record = recordManager.load(recordFilter.getSurvey(), recordId, step);
						writeValidationReport(record);
						status.incrementProcessed();
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
				LOG.error(message, e);
			}
		}
	}

	protected void initWriter() throws UnsupportedEncodingException {
		switch (reportType) {
		case CSV:
			csvWriter = new CsvWriter(outputStream, OpenForisIOUtils.UTF_8, ',', '"');
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
		RecordValidationReportGenerator reportGenerator = new RecordValidationReportGenerator(record);
		List<RecordValidationReportItem> validationItems = reportGenerator.generateValidationItems(
				locale, ValidationResultFlag.ERROR, includeConfirmedErrors);
		for (RecordValidationReportItem item : validationItems) {
			writeValidationReportLine(record, item);
		}
	}
	
	protected void writeHeader() throws IOException {
		switch (reportType) {
		case CSV:
			csvWriter.writeHeaders(VALIDATION_REPORT_HEADERS);
		break;
		}
	}

	protected void writeValidationReportLine(CollectRecord record, RecordValidationReportItem item) {
		String recordKey = validationMessageBuilder.getRecordKey(record);
		String phase = record.getStep().name();
		String absolutePath = Path.getAbsolutePath(item.getPath());
		NodeDefinition nodeDef = record.getSurvey().getSchema().getDefinitionByPath(absolutePath);
		String[] line = new String[]{recordKey, phase, nodeDef.getPath(), item.getPath(), item.getPrettyFormatPath(), item.getMessage()};
		switch (reportType) {
		case CSV:
			csvWriter.writeNext(line);
			break;
		}
	}

}
