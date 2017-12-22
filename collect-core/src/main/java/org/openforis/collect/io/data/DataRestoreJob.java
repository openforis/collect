/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyBackupJob.OutputFormat;
import org.openforis.collect.io.data.DataRestoreTask.OverwriteStrategy;
import org.openforis.collect.io.data.backup.BackupStorageManager;
import org.openforis.collect.io.data.restore.RestoredBackupStorageManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.RecordFilter;
import org.openforis.concurrency.Task;
import org.openforis.concurrency.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component(value=DataRestoreJob.JOB_NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DataRestoreJob extends DataRestoreBaseJob {
	
	public static final String JOB_NAME = "dataRestoreJob";
	
	@Autowired
	protected RestoredBackupStorageManager restoredBackupStorageManager;
	@Autowired
	protected BackupStorageManager backupStorageManager;

	//input parameters
	private boolean restoreUploadedFiles;
	private List<Integer> entryIdsToImport; //ignored when overwriteAll is true
	private boolean storeRestoredFile;
	private File tempFile;
	private boolean deleteAllRecordsBeforeRestore = false;
	private OverwriteStrategy recordOverwriteStrategy = OverwriteStrategy.ONLY_SPECIFIED;

	//output
	private List<RecordImportError> errors;
	
	//transient variables
	private transient List<File> recordFilesToBeDeleted;

	@Override
	public void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		this.errors = new ArrayList<RecordImportError>();
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		super.buildTasks();
		if (storeRestoredFile) {
			if (isBackupNeeded()) {
				addTask(SurveyBackupJob.class);
			}
			addTask(new StoreBackupFileTask());
		}
		if ( restoreUploadedFiles && recordFilesToBeDeleted == null ) {
			addTask(new RecordFileEnumeratorTask());
		}
		if (deleteAllRecordsBeforeRestore) {
			addTask(new DeleteRecordsTask());
		}
		addTask(DataRestoreTask.class);
		
		if (restoreUploadedFiles) {
			addTask(new RecordFileDeleteTask());
		}
		if ( restoreUploadedFiles && isUploadedFilesIncluded() ) {
			addTask(RecordFileRestoreTask.class);
		}
	}
	
	private boolean isBackupNeeded() {
		if (newSurvey) {
			return false;
		}
		Date lastBackupDate = backupStorageManager.getLastBackupDate(surveyName);
		RecordFilter recordFilter = new RecordFilter(publishedSurvey);
		recordFilter.setModifiedSince(lastBackupDate);
		return recordManager.countRecords(recordFilter) > 0 || 
				(lastBackupDate != null && publishedSurvey.getModifiedDate().after(lastBackupDate));
	}

	private boolean isUploadedFilesIncluded() throws IOException {
		List<String> dataEntries = backupFileExtractor.listEntriesInPath(SurveyBackupJob.UPLOADED_FILES_FOLDER);
		return ! dataEntries.isEmpty();
	}

	@Override
	protected void initializeTask(Worker task) {
		if (task instanceof SurveyBackupJob) {
			SurveyBackupJob t = (SurveyBackupJob) task;
			t.setFull(true);
			t.setIncludeData(true);
			t.setIncludeRecordFiles(true);
			t.setOutputFormat(OutputFormat.DESKTOP_FULL);
			t.setRecordFilter(new RecordFilter(publishedSurvey));
			t.setSurvey(publishedSurvey);
		} else if ( task instanceof DataRestoreTask ) {
			DataRestoreTask t = (DataRestoreTask) task;
			t.setRecordManager(recordManager);
			t.setUserManager(userManager);
			t.setUserGroupManager(userGroupManager);
			t.setRecordProvider(recordProvider);
			t.setTargetSurvey(publishedSurvey);
			t.setUser(user);
			t.setOverwriteStrategy(recordOverwriteStrategy);
			t.setEntryIdsToImport(entryIdsToImport);
			t.setIncludeRecordPredicate(includeRecordPredicate);
		} else if ( task instanceof RecordFileRestoreTask ) {
			RecordFileRestoreTask t = (RecordFileRestoreTask) task;
			t.setRecordManager(recordManager);
			t.setRecordFileManager(recordFileManager);
			t.setBackupFileExtractor(backupFileExtractor);
			t.setRecordProvider(recordProvider);
			t.setOverwriteStrategy(recordOverwriteStrategy);
			t.setEntryIdsToImport(entryIdsToImport);
			t.setSurvey(publishedSurvey);
		}
		super.initializeTask(task);
	}
	
	@Override
	protected void onTaskCompleted(Worker task) {
		super.onTaskCompleted(task);
		if (task instanceof RecordFileEnumeratorTask) {
			this.recordFilesToBeDeleted = ((RecordFileEnumeratorTask) task).getResult();
		} else if (task instanceof DataRestoreTask) {
			this.errors.addAll(((DataRestoreTask) task).getErrors());
		}
	}
	
	@Override
	protected void onCompleted() {
		super.onCompleted();
		if (storeRestoredFile) {
			restoredBackupStorageManager.moveToFinalFolder(surveyName, tempFile);
		}
	}
	
	@Override
	protected void onEnd() {
		super.onEnd();
		if (recordProvider instanceof Closeable) {
			IOUtils.closeQuietly((Closeable) recordProvider);
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

	public UserManager getUserManager() {
		return userManager;
	}
	
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public List<Integer> getEntryIdsToImport() {
		return entryIdsToImport;
	}
	
	public void setEntryIdsToImport(List<Integer> entryIdsToImport) {
		this.entryIdsToImport = entryIdsToImport;
	}
	
	public boolean isRestoreUploadedFiles() {
		return restoreUploadedFiles;
	}
	
	public void setRestoreUploadedFiles(boolean restoreUploadedFiles) {
		this.restoreUploadedFiles = restoreUploadedFiles;
	}

	public OverwriteStrategy getRecordOverwriteStrategy() {
		return recordOverwriteStrategy;
	}
	
	public void setRecordOverwriteStrategy(OverwriteStrategy recordOverwriteStrategy) {
		this.recordOverwriteStrategy = recordOverwriteStrategy;
	}
	
	public boolean isStoreRestoredFile() {
		return storeRestoredFile;
	}
	
	public void setStoreRestoredFile(boolean storeRestoredFile) {
		this.storeRestoredFile = storeRestoredFile;
	}
	
	public List<RecordImportError> getErrors() {
		return errors;
	}

	public void setDeleteAllRecordsBeforeRestore(boolean deleteAllRecords) {
		this.deleteAllRecordsBeforeRestore = deleteAllRecords;
	}

	public void setRecordFilesToBeDeleted(List<File> files) {
		this.recordFilesToBeDeleted = files;
	}
	
	private class StoreBackupFileTask extends Task {
		protected void execute() throws Throwable {
			DataRestoreJob.this.tempFile = restoredBackupStorageManager.storeTemporaryFile(surveyName, file);
		}
	}
	
	private class DeleteRecordsTask extends Task {
		protected void execute() throws Throwable {
			recordManager.deleteBySurvey(publishedSurvey.getId());
		}
	}

	private class RecordFileEnumeratorTask extends Task {
		
		private List<File> result;
		
		@Override
		protected void initializeInternalVariables() throws Throwable {
			super.initializeInternalVariables();
			this.result = new ArrayList<File>();
		}
		
		@Override
		protected long countTotalItems() {
			List<Integer> entryIds = calculateEntryIdsToImport();
			return entryIds.size() * Step.values().length;
		}
		
		@Override
		protected void execute() throws Throwable {
			boolean originalRecordValidationSetting = ((XMLParsingRecordProvider) recordProvider).isValidateRecords();
			((XMLParsingRecordProvider) recordProvider).setValidateRecords(false);
			List<Integer> entryIds = calculateEntryIdsToImport();
			for (Step step : Step.values()) {
				for (Integer entryId : entryIds) {
					CollectRecord packagedRecord = recordProvider.provideRecord(entryId, step);
					if (packagedRecord != null) {
						int rootEntityId = packagedRecord.getRootEntity().getDefinition().getId();
						CollectRecordSummary existingRecordSummary = recordManager.loadUniqueRecordSummaryByKeys(publishedSurvey, rootEntityId, packagedRecord.getRootEntityKeyValues());
						if (existingRecordSummary != null && existingRecordSummary.getStep().afterEqual(step)) {
							CollectRecord record = recordManager.load(publishedSurvey, existingRecordSummary.getId(), step, false);
							List<File> files = recordFileManager.getAllFiles(record);
							result.addAll(files);
						}
					}
					incrementProcessedItems();
				}
			}
			((XMLParsingRecordProvider) recordProvider).setValidateRecords(originalRecordValidationSetting);
		}
		
		private List<Integer> calculateEntryIdsToImport() {
			if ( entryIdsToImport != null ) {
				return entryIdsToImport;
			} else {
				return recordProvider.findEntryIds();
			}
		}
		
		public List<File> getResult() {
			return result;
		}
	}
	
	private class RecordFileDeleteTask extends Task {
		
		@Override
		protected void execute() throws Throwable {
			for (File file : recordFilesToBeDeleted) {
				if (file.exists() && ! file.delete()) {
					throw new RuntimeException("Error deleting record file: " + file.getAbsolutePath());
				}
			}
		}
	}

}
