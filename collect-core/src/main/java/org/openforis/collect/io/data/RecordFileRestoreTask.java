/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.io.NewBackupFileExtractor;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.BackupDataExtractor.BackupRecordEntry;
import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionRecordFileManager;
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
	private File file;
	private CollectSurvey survey;
	private List<Integer> entryIdsToImport;
	private boolean overwriteAll;
	
	//temporary instance variables
	private SessionRecordFileManager sessionRecordFileManager;
	private List<Integer> processedRecords;
	private NewBackupFileExtractor backupFileExtractor;
	private boolean oldBackupFormat;
	
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
		backupFileExtractor = new NewBackupFileExtractor(file);
		backupFileExtractor.init();
		List<Integer> idsToImport = calculateEntryIdsToImport();
		for (Integer entryId : idsToImport) {
			if ( isRunning() && ! processedRecords.contains(entryId) ) {
				importRecordFiles(entryId);
				processedRecords.add(entryId);
				incrementItemsProcessed();
			} else {
				break;
			}
		}
	}
	
	private List<Integer> calculateEntryIdsToImport() {
		if ( entryIdsToImport != null ) {
			return entryIdsToImport;
		} 
		if ( ! overwriteAll ) {
			throw new IllegalArgumentException("No entries to import specified and overwriteAll parameter is 'false'");
		}
		Set<Integer> result = new TreeSet<Integer>();
		for (Step step : Step.values()) {
			int stepNumber = step.getStepNumber();
			String path = SurveyBackupJob.DATA_FOLDER + SurveyBackupJob.ZIP_FOLDER_SEPARATOR + stepNumber;
			if ( backupFileExtractor.containsEntriesInPath(path) ) {
				List<String> listEntriesInPath = backupFileExtractor.listFilesInFolder(path);
				for (String entry : listEntriesInPath) {
					String entryId = FilenameUtils.getBaseName(entry);
					result.add(Integer.parseInt(entryId));
				}
			}
		}
		return new ArrayList<Integer>(result);
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
		String[] recordKeys = lastStepBackupRecord.getRootEntityKeyValues().toArray(new String[lastStepBackupRecord.getRootEntityKeyValues().size()]);
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, lastStepBackupRecord.getRootEntity().getName(), recordKeys);
		if ( summaries.size() == 1 ) {
			CollectRecord summary = summaries.get(0);
			CollectRecord storedRecord = recordManager.load(survey, summary.getId(), summary.getStep(), false);
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
			BackupDataExtractor backupDataExtractor = new BackupDataExtractor(survey, file, step);
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
		sessionRecordFileManager.resetTempInfo();
		recordFileManager.deleteAllFiles(record);
		List<FileAttribute> fileAttributes = record.getFileAttributes();
		for (FileAttribute fileAttribute : fileAttributes) {
			String recordFileEntryName = RecordFileBackupTask.calculateRecordFileEntryName(fileAttribute);
			InputStream is = backupFileExtractor.findEntryInputStream(recordFileEntryName);
			if ( is != null ) {
				sessionRecordFileManager.saveToTempFile(is, fileAttribute.getFilename(), 
						record, fileAttribute.getInternalId());
			}
		}
		if ( sessionRecordFileManager.commitChanges(record) ) {
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

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
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
