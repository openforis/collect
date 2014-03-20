/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.IOException;
import java.util.List;

import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyRestoreJob.BackupFileExtractor;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DataRestoreJob extends DataRestoreBaseJob {
	
	private RecordFileManager recordFileManager;
	
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private UserManager userManager;

	//parameters
	private boolean overwriteAll;
	private boolean restoreUploadedFiles;
	private List<Integer> entryIdsToImport;

	@Override
	protected void buildTasks() throws Throwable {
		super.buildTasks();
		addTask(DataRestoreTask.class);
		if ( restoreUploadedFiles && isUploadedFilesIncluded() ) {
			addTask(RecordFileRestoreTask.class);
		}
	}
	
	private boolean isUploadedFilesIncluded() throws IOException {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
		List<String> dataEntries = backupFileExtractor.listEntriesInPath(SurveyBackupJob.UPLOADED_FILES_FOLDER);
		return ! dataEntries.isEmpty();
	}

	@Override
	protected void prepareTask(Task task) {
		if ( task instanceof DataRestoreTask ) {
			DataRestoreTask t = (DataRestoreTask) task;
			t.setRecordManager(recordManager);
			t.setUserManager(userManager);
			t.setEntryBasePath(SurveyBackupJob.DATA_FOLDER);
			t.setZipFile(zipFile);
			t.setPackagedSurvey(packagedSurvey);
			t.setExistingSurvey(publishedSurvey);
			t.setOverwriteAll(overwriteAll);
			t.setEntryIdsToImport(entryIdsToImport);
		} else if ( task instanceof RecordFileRestoreTask ) {
			RecordFileRestoreTask t = (RecordFileRestoreTask) task;
			t.setRecordManager(recordManager);
			t.setRecordFileManager(recordFileManager);
			t.setZipFile(zipFile);
			t.setOverwriteAll(overwriteAll);
			t.setEntryIdsToImport(entryIdsToImport);
			t.setSurvey(publishedSurvey);
		}
		super.prepareTask(task);
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

	public boolean isOverwriteAll() {
		return overwriteAll;
	}

	public void setOverwriteAll(boolean overwriteAll) {
		this.overwriteAll = overwriteAll;
	}

}
