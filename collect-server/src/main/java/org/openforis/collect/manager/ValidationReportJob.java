package org.openforis.collect.manager;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.util.IOUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordValidationReportGenerator;
import org.openforis.collect.model.RecordValidationReportItem;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.commons.collection.Visitor;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.path.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ValidationReportJob extends Job {

	private static final String[] VALIDATION_REPORT_HEADERS = new String[] { "Record", "Phase", "Attribute Schema Path",
			"Field path", "Field path (labels)", "Error message", "Severity" };

	public enum ReportType {
		CSV
	}

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private MessageSource messageSource;

	private Input input;

	private ValidationMessageBuilder validationMessageBuilder;
	private CsvWriter csvWriter;
	private OutputStream outputStream;
	private File outputFile;

	@Override
	protected void initializeInternalVariables() throws Throwable {
		super.initializeInternalVariables();

		validationMessageBuilder = ValidationMessageBuilder.createInstance(messageSource);
		outputFile = File.createTempFile("of_collect_validation_report", ".csv");
		outputStream = new FileOutputStream(outputFile);

		if (input.reportType == ReportType.CSV) {
			csvWriter = new CsvWriter(outputStream, IOUtils.UTF_8, ',', '"');
		}
	}

	@Override
	protected void buildTasks() throws Throwable {
		ValidationReportTask task = new ValidationReportTask();
		addTask(task);
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		if (input.reportType == ReportType.CSV) {
			org.apache.commons.io.IOUtils.closeQuietly(csvWriter);
		}
	}

	public Input getInput() {
		return this.input;
	}

	public void setInput(Input input) {
		this.input = input;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public static class Input {

		private ReportType reportType = ReportType.CSV;
		private RecordFilter recordFilter;
		private boolean includeConfirmedErrors = true;
		private Locale locale = Locale.ENGLISH;

		public ReportType getReportType() {
			return reportType;
		}

		public void setReportType(ReportType reportType) {
			this.reportType = reportType;
		}

		public RecordFilter getRecordFilter() {
			return recordFilter;
		}

		public void setRecordFilter(RecordFilter recordFilter) {
			this.recordFilter = recordFilter;
		}

		public boolean isIncludeConfirmedErrors() {
			return includeConfirmedErrors;
		}

		public void setIncludeConfirmedErrors(boolean includeConfirmedErrors) {
			this.includeConfirmedErrors = includeConfirmedErrors;
		}

		public Locale getLocale() {
			return locale;
		}

		public void setLocale(Locale locale) {
			this.locale = locale;
		}
	}

	private class ValidationReportTask extends Task {

		@Override
		protected void execute() throws Throwable {
			writeHeader();

			CollectSurvey survey = input.recordFilter.getSurvey();

			recordManager.visitSummaries(input.recordFilter, null, new Visitor<CollectRecordSummary>() {
				public void visit(CollectRecordSummary summary) {
					if (isRunning()) {
						try {
							Step step = summary.getStep();
							Integer recordId = summary.getId();
							CollectRecord record = recordManager.load(survey, recordId, step);
							writeValidationReport(record);
							incrementProcessedItems();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			});
		}

		protected void writeValidationReport(CollectRecord record) throws IOException {
			RecordValidationReportGenerator reportGenerator = new RecordValidationReportGenerator(record);
			List<RecordValidationReportItem> validationItems = reportGenerator.generateValidationItems(input.locale,
					ValidationResultFlag.WARNING, input.includeConfirmedErrors);
			for (RecordValidationReportItem item : validationItems) {
				writeValidationReportLine(record, item);
			}
		}

		protected void writeHeader() throws IOException {
			if (input.reportType == ReportType.CSV) {
				csvWriter.writeHeaders(VALIDATION_REPORT_HEADERS);
			}
		}

		protected void writeValidationReportLine(CollectRecord record, RecordValidationReportItem item) {
			String recordKey = validationMessageBuilder.getRecordKey(record);
			String phase = record.getStep().name();
			String absolutePath = Path.getAbsolutePath(item.getPath());
			NodeDefinition nodeDef = record.getSurvey().getSchema().getDefinitionByPath(absolutePath);
			String[] line = new String[] { recordKey, phase, nodeDef.getPath(), item.getPath(),
					item.getPrettyFormatPath(), item.getMessage(),
					item.getSeverity().name().toLowerCase(Locale.ENGLISH) };
			if (input.reportType == ReportType.CSV) {
				csvWriter.writeNext(line);
			}
		}
	}

}
