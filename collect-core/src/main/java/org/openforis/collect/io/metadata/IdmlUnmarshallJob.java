/**
 * 
 */
package org.openforis.collect.io.metadata;

import java.io.File;
import java.util.zip.ZipFile;

import org.openforis.collect.io.SurveyRestoreJob.BackupFileExtractor;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IdmlUnmarshallJob extends Job {

	//input
	private File file;
	private boolean validate;

	//output
	private CollectSurvey survey;
	
	public void configure(File file, boolean validate) {
		super.configure();
		this.file = file;
		this.validate = validate;
	}

	@Override
	protected void buildAndAddTasks() throws Throwable {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
			File idmlFile = backupFileExtractor.extractIdmlFile();
			zipFile.close();
		
			final IdmlUnmarshallTask task = createTask(IdmlUnmarshallTask.class);
			task.setFile(idmlFile);
			task.setValidate(validate);
			addTask(task);
		} catch ( Exception e ) {
			throw new RuntimeException("Error configuring job: " + e.getMessage(), e);
		}
	}
	
	@Override
	protected void onTaskCompleted(Task task) {
		super.onTaskCompleted(task);
		if ( task instanceof IdmlUnmarshallTask ) {
			IdmlUnmarshallJob.this.survey = ((IdmlUnmarshallTask) task).getSurvey();
		}
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
}
