/**
 * 
 */
package org.openforis.collect.io.data;

import java.util.List;

import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyRestoreJob.BackupFileExtractor;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
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
	private transient SurveyManager surveyManager;
	
	//output
	protected DataImportSummary summary;
	
	@Override
	public void initInternal() throws Throwable {
		super.initInternal();
		if ( ! isDataIncluded() ) {
			throw new RuntimeException("No data found in backup file");
		}
	}

	@Override
	protected void buildAndAddTasks() throws Throwable {
		super.buildAndAddTasks();
		addTask(DataRestoreSummaryTask.class);
	}
	
	@Override
	protected void prepareTask(Task task) {
		super.prepareTask(task);
		if ( task instanceof DataRestoreSummaryTask ) {
			DataRestoreSummaryTask t = (DataRestoreSummaryTask) task;
			t.setZipFile(zipFile);
			t.setEntryPrefix(SurveyBackupJob.DATA_FOLDER);
			t.setPackagedSurvey(packagedSurvey);
			t.setExistingSurvey(publishedSurvey);
			t.setPackagedSurvey(DataRestoreSummaryJob.this.packagedSurvey);
		}
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

	private boolean isDataIncluded() {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
		List<String> dataEntries = backupFileExtractor.listEntriesInPath(SurveyBackupJob.DATA_FOLDER);
		return ! dataEntries.isEmpty();
	}

	public DataImportSummary getSummary() {
		return summary;
	}

}
