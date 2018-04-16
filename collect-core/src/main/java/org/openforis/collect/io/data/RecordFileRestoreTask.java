/**
 * 
 */
package org.openforis.collect.io.data;

import static org.openforis.collect.io.data.DataRestoreTask.OverwriteStrategy.ONLY_SPECIFIED;
import static org.openforis.collect.io.data.DataRestoreTask.OverwriteStrategy.OVERWRITE_ALL;
import static org.openforis.collect.io.data.DataRestoreTask.OverwriteStrategy.OVERWRITE_OLDER;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.data.DataRestoreTask.OverwriteStrategy;
import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionRecordFileManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.concurrency.Task;
import org.openforis.idm.model.FileAttribute;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RecordFileRestoreTask extends Task {
	
	//managers
	private RecordManager recordManager;
	private RecordFileManager recordFileManager;
	
	//input
	private CollectSurvey survey;
	private List<Integer> entryIdsToImport;
	private OverwriteStrategy overwriteStrategy = OverwriteStrategy.ONLY_SPECIFIED;
	private RecordProvider recordProvider;
	private BackupFileExtractor backupFileExtractor;
	
	//temporary instance variables
	private SessionRecordFileManager sessionRecordFileManager;
	private List<Integer> processedRecords;
	
	public RecordFileRestoreTask() {
		sessionRecordFileManager = new SessionRecordFileManager();
	}
	
	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		sessionRecordFileManager.setRecordFileManager(recordFileManager);
	}
	
	@Override
	protected void execute() throws Throwable {
		processedRecords = new ArrayList<Integer>();
		List<Integer> idsToImport = calculateEntryIdsToImport();
		for (Integer entryId : idsToImport) {
			if ( isRunning() && ! processedRecords.contains(entryId) ) {
				importRecordFiles(entryId);
				processedRecords.add(entryId);
				incrementProcessedItems();
			} else {
				break;
			}
		}
	}
	
	private List<Integer> calculateEntryIdsToImport() {
		if ( entryIdsToImport != null ) {
			return entryIdsToImport;
		} 
		return recordProvider.findEntryIds();
	}

	private void importRecordFiles(int entryId) throws IOException, DataImportExeption, RecordPersistenceException, RecordParsingException {
		CollectRecord lastStepBackupRecord = getLastStepBackupRecord(entryId);
		if ( lastStepBackupRecord == null ) {
			throw new IllegalStateException("Error parsing record for entry: " + entryId);
		}
		CollectRecordSummary storedRecordSummary = findStoredRecordSummary(lastStepBackupRecord);
		if (overwriteStrategy == OVERWRITE_OLDER && isNewer(lastStepBackupRecord, storedRecordSummary)
				|| overwriteStrategy == ONLY_SPECIFIED || overwriteStrategy == OVERWRITE_ALL) {
			CollectRecord storedRecord = recordManager.load(survey, storedRecordSummary.getId(), storedRecordSummary.getStep(), false);
			importRecordFiles(storedRecord);
		}
	}

	private CollectRecordSummary findStoredRecordSummary(CollectRecord record) {
		List<String> recordKeys = record.getRootEntityKeyValues();
		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(record.getRootEntityDefinitionId());
		filter.setKeyValues(recordKeys);
		List<CollectRecordSummary> summaries = recordManager.loadSummaries(filter);
		if ( summaries.size() == 1 ) {
			CollectRecordSummary summary = summaries.get(0);
			return summary;
		} else if ( summaries.size() == 0 ) {
			throw new RuntimeException(String.format("Record with keys %s not found", recordKeys.toString()));
		} else {
			throw new RuntimeException(String.format("Multiple records found with keys %s not found", recordKeys.toString()));
		}
	}
	
	private CollectRecord getLastStepBackupRecord(int entryId) throws IOException, RecordParsingException {
		Step[] steps = Step.values();
		for (int i = steps.length - 1; i >= 0; i--) {
			Step step = steps[i];
			CollectRecord record = recordProvider.provideRecord(entryId, step);
			if (record != null) {
				return record;
			}
		}
		return null;
	}
	
	private boolean isNewer(CollectRecord record, CollectRecordSummary existingSummary) {
		Date existingRecordModifiedDate = existingSummary.getSummaryByStep(record.getDataStep()).getModifiedDate();
		return record.getModifiedDate() != null && 
				(existingRecordModifiedDate == null || 
					record.getModifiedDate().compareTo(existingRecordModifiedDate) > 0
				);
	}
	
	private void importRecordFiles(CollectRecord record) throws IOException, RecordPersistenceException {
		sessionRecordFileManager.resetTempInfo();
		//recordFileManager.deleteAllFiles(record);
		sessionRecordFileManager.prepareDeleteAllFiles(record);
		List<FileAttribute> fileAttributes = record.getFileAttributes();
		for (FileAttribute fileAttribute : fileAttributes) {
			if (! fileAttribute.isEmpty()) {
				String recordFileEntryName = RecordFileBackupTask.determineRecordFileEntryName(fileAttribute);
				InputStream is = backupFileExtractor.findEntryInputStream(recordFileEntryName);
				if ( is != null ) {
					sessionRecordFileManager.saveToTempFile(is, fileAttribute.getFilename(), 
							record, fileAttribute.getInternalId());
				}
			}
		}
		if ( sessionRecordFileManager.commitChanges(record) ) {
			recordManager.save(record, null, null, false);
		}
	}

	public RecordManager getRecordManager() {
		return recordManager;
	}

	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}

	public RecordFileManager getRecordFileManager() {
		return recordFileManager;
	}

	public void setRecordFileManager(RecordFileManager recordFileManager) {
		this.recordFileManager = recordFileManager;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public List<Integer> getEntryIdsToImport() {
		return entryIdsToImport;
	}

	public void setEntryIdsToImport(List<Integer> entryIdsToImport) {
		this.entryIdsToImport = entryIdsToImport;
	}

	public OverwriteStrategy getOverwriteStrategy() {
		return overwriteStrategy;
	}
	
	public void setOverwriteStrategy(OverwriteStrategy overwriteStrategy) {
		this.overwriteStrategy = overwriteStrategy;
	}
	
	public void setBackupFileExtractor(BackupFileExtractor backupFileExtractor) {
		this.backupFileExtractor = backupFileExtractor;
	}
	
	public void setRecordProvider(RecordProvider recordProvider) {
		this.recordProvider = recordProvider;
	}
	
}
