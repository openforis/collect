package org.openforis.collect.io.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.BackupDataExtractor.BackupRecordEntry;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.ZipFiles;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DataBackupTask extends Task {

	private static final String[] DATA_SUMMARY_FIXED_HEADERS = new String[]{
			"entry_id", "root_entity_id", "step", "created_on", "created_by", 
			"last_modified", "modified_by"};
	private static final String DATA_SUMMARY_RECORD_KEY_PREFIX = "key";
	
	private RecordManager recordManager;
	private DataMarshaller dataMarshaller;
	
	//input
	private ZipOutputStream zipOutputStream;
	private CollectSurvey survey;
	private Step[] steps;
	private RecordFilter recordFilter;
	private List<DataBackupError> errors;
	
	//internal
	private CsvWriter summaryCSVWriter;
	private File summaryTempFile;
	
	public DataBackupTask() {
		super();
		//export all steps by default
		this.steps = Step.values();
		this.errors = new ArrayList<DataBackupError>();
	}
	
	@Override
	protected void createInternalVariables() throws Throwable {
		if ( recordFilter == null ) {
			recordFilter = new RecordFilter(survey);
		}
		initializeDataSummaryCSVWriter();
		super.createInternalVariables();
	}

	private void initializeDataSummaryCSVWriter()
			throws IOException, UnsupportedEncodingException, FileNotFoundException {
		summaryTempFile = File.createTempFile("summary", ".csv");
		summaryCSVWriter = new CsvWriter(new FileOutputStream(summaryTempFile), OpenForisIOUtils.UTF_8, ',', '"');
		List<String> headers = new ArrayList<String>(Arrays.asList(DATA_SUMMARY_FIXED_HEADERS));
		for (int i = 0; i < SurveyValidator.MAX_KEY_ATTRIBUTE_DEFINITION_COUNT; i++) {
			headers.add(DATA_SUMMARY_RECORD_KEY_PREFIX + (i + 1));
		}
		summaryCSVWriter.writeHeaders(headers);
	}

	@Override
	protected long countTotalItems() {
		int count = 0;
		List<CollectRecordSummary> recordSummaries = recordManager.loadSummaries(recordFilter);
		if ( CollectionUtils.isNotEmpty(recordSummaries) && steps != null && steps.length > 0 ) {
			for (CollectRecordSummary summary : recordSummaries) {
				for (Step step : steps) {
					if ( step.getStepNumber() <= summary.getStep().getStepNumber() ) {
						count ++;
					}
				}
			}
		}
		return count;
	}

	@Override
	protected void execute() throws Throwable {
		List<CollectRecordSummary> recordSummaries = recordManager.loadSummaries(recordFilter);
		if ( CollectionUtils.isNotEmpty(recordSummaries) && steps != null && steps.length > 0 ) {
			for (CollectRecordSummary summary : recordSummaries) {
				for (Step step : steps) {
					if ( isRunning() ) {
						if ( step.getStepNumber() <= summary.getStep().getStepNumber() ) {
							backup(summary, step);
							incrementProcessedItems();
						}
					} else {
						break;
					}
				}
				if (isRunning()) {
					writeSummaryEntry(summary);
				}
			}
		}
	}
	
	@Override
	protected void onEnd() {
		IOUtils.closeQuietly(summaryCSVWriter);
		ZipFiles.writeFile(zipOutputStream, summaryTempFile, SurveyBackupJob.DATA_SUMMARY_ENTRY_NAME);
		super.onEnd();
	}

	private void backup(CollectRecordSummary summary, Step step) {
		Integer id = summary.getId();
		try {
			CollectRecord record = recordManager.load(survey, id, step, false);
			BackupRecordEntry recordEntry = new BackupRecordEntry(step, id);
			ZipEntry entry = new ZipEntry(recordEntry.getName());
			zipOutputStream.putNextEntry(entry);
			OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream);
			dataMarshaller.write(record, writer);
			zipOutputStream.closeEntry();
		} catch (Exception e) {
			DataBackupError error = new DataBackupError(summary.getId(), summary.getRootEntityKeyValues(), 
					summary.getStep(), e.getMessage());
			errors.add(error);
			log().error(error.toString(), e);
		}
	}

	private void writeSummaryEntry(CollectRecordSummary summary) {
		Step lastStep = null;
		for (Step step : steps) {
			if ( step.getStepNumber() <= summary.getStep().getStepNumber() ) {
				lastStep = step;
			}
		}
		List<String> values = new ArrayList<String>(Arrays.asList(
				String.valueOf(summary.getId()),
				summary.getRootEntityDefinitionId().toString(),
				lastStep.name(), 
				Dates.formatDateTime(summary.getCreationDate()),
				summary.getCreatedBy() == null ? "" : summary.getCreatedBy().getUsername(),
				Dates.formatDateTime(summary.getModifiedDate()),
				summary.getModifiedBy() == null ? "" : summary.getModifiedBy().getUsername()
		));
		values.addAll(summary.getRootEntityKeyValues());
		summaryCSVWriter.writeNext(values);
	}
	
	public RecordManager getRecordManager() {
		return recordManager;
	}
	
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public DataMarshaller getDataMarshaller() {
		return dataMarshaller;
	}
	
	public void setDataMarshaller(DataMarshaller dataMarshaller) {
		this.dataMarshaller = dataMarshaller;
	}
	
	public RecordFilter getRecordFilter() {
		return recordFilter;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public Step[] getSteps() {
		return steps;
	}
	
	public void setSteps(Step[] steps) {
		this.steps = steps;
	}

	public ZipOutputStream getZipOutputStream() {
		return zipOutputStream;
	}

	public void setZipOutputStream(ZipOutputStream zipOutputStream) {
		this.zipOutputStream = zipOutputStream;
	}

	public List<DataBackupError> getErrors() {
		return errors;
	}
	
}
