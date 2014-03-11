package org.openforis.collect.io.data;

import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openforis.collect.io.data.BackupDataExtractor.BackupRecordEntry;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataMarshaller;
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

	private RecordManager recordManager;
	private DataMarshaller dataMarshaller;
	
	//input
	private ZipOutputStream zipOutputStream;
	private CollectSurvey survey;
	private Step[] steps;
	private String rootEntityName;
	
	public DataBackupTask() {
		super();
		//export all steps by default
		this.steps = Step.values();
	}
	
	@Override
	protected long countTotalItems() {
		int count = 0;
		List<CollectRecord> recordSummaries = loadAllSummaries();
		for (CollectRecord summary : recordSummaries) {
			for (Step step: steps) {
				if ( step.getStepNumber() <= summary.getStep().getStepNumber() ) {
					count ++;
				}
			}
		}
		return count;
	}

	@Override
	protected void execute() throws Throwable {
		List<CollectRecord> recordSummaries = loadAllSummaries();
		if ( recordSummaries != null && steps != null && steps.length > 0 ) {
			for (CollectRecord summary : recordSummaries) {
				if ( isRunning() ) {
					for (Step step : steps) {
						int stepNum = step.getStepNumber();
						if ( stepNum <= summary.getStep().getStepNumber() ) {
							backup(summary, Step.valueOf(stepNum));
							incrementItemsProcessed();
						}
					}
				} else {
					break;
				}
			}
		}
	}

	private List<CollectRecord> loadAllSummaries() {
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName);
		return summaries;
	}
	
	private void backup(CollectRecord summary, Step step) {
		Integer id = summary.getId();
		try {
			CollectRecord record = recordManager.load(survey, id, step);
			BackupRecordEntry recordEntry = new BackupRecordEntry(rootEntityName, step, id);
			String entryName = recordEntry.getName();
			ZipEntry entry = new ZipEntry(entryName);
			zipOutputStream.putNextEntry(entry);
			OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream);
			dataMarshaller.write(record, writer);
			zipOutputStream.closeEntry();
		} catch (Exception e) {
			String message = "Error while backing up " + id + " " + e.getMessage();
			throw new RuntimeException(message, e);
		}
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
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public String getRootEntityName() {
		return rootEntityName;
	}
	
	public void setRootEntityName(String rootEntityName) {
		this.rootEntityName = rootEntityName;
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

}
