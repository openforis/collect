/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.data.BackupDataExtractor.BackupRecordEntry;
import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
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
	private ZipFile zipFile;
	private CollectSurvey survey;
	private List<Integer> entryIdsToImport;
	private boolean overwriteAll;
	
	//temporary instance variables
	private List<Integer> processedRecords;
	private BackupFileExtractor backupFileExtractor;
	private boolean oldBackupFormat;
	
	@Override
	protected void execute() throws Throwable {
		processedRecords = new ArrayList<Integer>();
		backupFileExtractor = new BackupFileExtractor(zipFile);
		for (Integer entryId : entryIdsToImport) {
			if ( isRunning() && ! processedRecords.contains(entryId) ) {
				importRecordFiles(entryId);
				processedRecords.add(entryId);
				incrementItemsProcessed();
			} else {
				break;
			}
		}
	}

	private void importRecordFiles(int entryId) throws IOException, DataImportExeption, RecordPersistenceException {
		CollectRecord lastStepBackupRecord = getLastStepBackupRecord(entryId);
		if ( lastStepBackupRecord == null ) {
			throw new IllegalStateException("Error parsing record for entry: " + entryId);
		}
		CollectRecord storedRecord = findStoredRecord(lastStepBackupRecord);
		importRecordFiles(storedRecord);
	}

	protected CollectRecord findStoredRecord(CollectRecord lastStepBackupRecord) {
		String[] recordKeys = lastStepBackupRecord.getRootEntityKeyValues().toArray(new String[]{});
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, lastStepBackupRecord.getRootEntity().getName(), recordKeys);
		if ( summaries.size() == 1 ) {
			CollectRecord summary = summaries.get(0);
			CollectRecord storedRecord = recordManager.load(survey, summary.getId(), summary.getStep());
			return storedRecord;
		} else if ( summaries.size() == 0 ) {
			throw new RuntimeException(String.format("Record with keys %s not found", recordKeys.toString()));
		} else {
			throw new RuntimeException(String.format("Multiple records found with keys %s not found", recordKeys.toString()));
		}
	}
	
	@SuppressWarnings("resource")
	protected CollectRecord getLastStepBackupRecord(int entryId) throws IOException {
		Step[] steps = Step.values();
		for (int i = steps.length - 1; i >= 0; i--) {
			Step step = steps[i];
			BackupRecordEntry recordEntry = new BackupRecordEntry(step, entryId, oldBackupFormat);
			BackupDataExtractor backupDataExtractor = new BackupDataExtractor(survey, zipFile, step);
			backupDataExtractor.init();
			ParseRecordResult parseRecordResult = backupDataExtractor.findRecord(recordEntry);
			if ( parseRecordResult != null ) {
				if ( parseRecordResult.isSuccess() ) {
					return parseRecordResult.getRecord();
				} else {
					log().error("Error parsing record for entry: " + recordEntry.getName());
					//TODO handle this error?
				}
			}
		}
		return null;
	}
	
	private void importRecordFiles(CollectRecord record) throws IOException, RecordPersistenceException {
		recordFileManager.resetTempInfo();
		recordFileManager.deleteAllFiles(record);
		List<FileAttribute> fileAttributes = record.getFileAttributes();
		String sessionId = "admindataimport";
		for (FileAttribute fileAttribute : fileAttributes) {
			String recordFileEntryName = RecordFileBackupTask.calculateRecordFileEntryName(fileAttribute);
			InputStream is = backupFileExtractor.findEntryInputStream(recordFileEntryName);
			if ( is != null ) {
				recordFileManager.saveToTempFolder(is, fileAttribute.getFilename(), 
						sessionId, record, fileAttribute.getInternalId());
			}
		}
		if ( recordFileManager.commitChanges(sessionId, record) ) {
			if ( record.getStep() == Step.ANALYSIS ) {
				record.setStep(Step.CLEANSING);
				recordManager.save(record);
				record.setStep(Step.ANALYSIS);
			}
			recordManager.save(record);
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

	public ZipFile getZipFile() {
		return zipFile;
	}

	public void setZipFile(ZipFile zipFile) {
		this.zipFile = zipFile;
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

	public boolean isOldBackupFormat() {
		return oldBackupFormat;
	}
	
	public void setOldBackupFormat(boolean oldBackupFormat) {
		this.oldBackupFormat = oldBackupFormat;
	}
	
	public boolean isOverwriteAll() {
		return overwriteAll;
	}

	public void setOverwriteAll(boolean overwriteAll) {
		this.overwriteAll = overwriteAll;
	}
	
}
