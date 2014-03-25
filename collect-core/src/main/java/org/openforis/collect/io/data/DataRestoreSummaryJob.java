/**
 * 
 */
package org.openforis.collect.io.data;

import java.util.List;

import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.Predicate;
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
public class DataRestoreSummaryJob extends DataRestoreBaseJob {

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private SurveyManager surveyManager;
	
	//input
	private Predicate<CollectRecord> includeRecordPredicate;
	
	//output
	private DataImportSummary summary;

	//transient
	private boolean oldFormat;
	
	@Override
	public void initInternal() throws Throwable {
		super.initInternal();
		oldFormat = ! isDataFolderIncluded();
	}

	@Override
	protected void buildTasks() throws Throwable {
		super.buildTasks();
		addTask(DataRestoreSummaryTask.class);
	}
	
	@Override
	protected void prepareTask(Task task) {
		if ( task instanceof DataRestoreSummaryTask ) {
			DataRestoreSummaryTask t = (DataRestoreSummaryTask) task;
			t.setRecordManager(recordManager);
			t.setUserManager(userManager);
			t.setZipFile(zipFile);
			t.setOldFormat(oldFormat);
			t.setPackagedSurvey(packagedSurvey);
			t.setExistingSurvey(publishedSurvey);
			t.setPackagedSurvey(DataRestoreSummaryJob.this.packagedSurvey);
			t.setIncludeRecordPredicate(includeRecordPredicate);
		}
		super.prepareTask(task);
	}
	
	@Override
	protected void onTaskCompleted(Task task) {
		super.onTaskCompleted(task);
		if ( task instanceof DataRestoreSummaryTask ) {
			//get output survey and set it into job instance instance variable
			DataRestoreSummaryJob.this.summary = ((DataRestoreSummaryTask) task).getSummary();
		} else if ( task instanceof IdmlUnmarshallTask ) {
			CollectSurvey survey = ((IdmlUnmarshallTask) task).getSurvey();
			if ( survey == null ) {
				throw new RuntimeException("Error extracting packaged survey");
			} else {
				packagedSurvey = survey;
				checkPackagedSurveyUri();
				initExistingSurvey();
			}
			DataRestoreSummaryJob.this.packagedSurvey = survey;
		}
	}

	private boolean isDataFolderIncluded() {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
		List<String> dataEntries = backupFileExtractor.listEntriesInPath(SurveyBackupJob.DATA_FOLDER);
		return ! dataEntries.isEmpty();
	}

	public UserManager getUserManager() {
		return userManager;
	}
	
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
	
	public RecordManager getRecordManager() {
		return recordManager;
	}
	
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public Predicate<CollectRecord> getIncludeRecordPredicate() {
		return includeRecordPredicate;
	}
	
	public void setIncludeRecordPredicate(
			Predicate<CollectRecord> includeRecordPredicate) {
		this.includeRecordPredicate = includeRecordPredicate;
	}
	
	public DataImportSummary getSummary() {
		return summary;
	}

}
