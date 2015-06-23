/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.File;
import java.util.zip.ZipFile;

import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Worker;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public abstract class DataRestoreBaseJob extends Job {

	@Autowired
	protected SurveyManager surveyManager;
	@Autowired
	protected RecordManager recordManager;
	@Autowired
	protected UserManager userManager;
	
	//input
	protected transient File file;
	protected transient CollectSurvey publishedSurvey;
	protected transient CollectSurvey packagedSurvey; //optional: if not specified, it will be extracted from zip file
	
	//temporary instance variables
	protected transient ZipFile zipFile;
	
	@Override
	protected void buildTasks() throws Throwable {
		if ( packagedSurvey == null ) {
			addIdmlUnmarshallTask();
		}
	}

	@Override
	public void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		zipFile = new ZipFile(file);
	}
	
	@Override
	protected void validateInput() throws Throwable {
		super.validateInput();
		if ( packagedSurvey != null ) {
			checkPackagedSurveyUri();
		}
	}

	private void addIdmlUnmarshallTask() {
		final IdmlUnmarshallTask task = createTask(IdmlUnmarshallTask.class);
		addTask(task);
	}
	
	@Override
	protected void initializeTask(Worker task) {
		if ( task instanceof IdmlUnmarshallTask ) {
			IdmlUnmarshallTask t = (IdmlUnmarshallTask) task;
			BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
			File idmlFile = backupFileExtractor.extractIdmlFile();
			t.setSurveyManager(surveyManager);
			t.setFile(idmlFile);
			t.setValidate(false);
		}
		super.initializeTask(task);
	}
	
	@Override
	protected void onTaskCompleted(Worker task) {
		super.onTaskCompleted(task);
		if ( task instanceof IdmlUnmarshallTask ) {
			CollectSurvey survey = ((IdmlUnmarshallTask) task).getSurvey();
			if ( survey == null ) {
				throw new RuntimeException("Error extracting packaged survey");
			} else {
				packagedSurvey = survey;
				checkPackagedSurveyUri();
			}
			this.packagedSurvey = survey;
		}
	}


	protected void checkPackagedSurveyUri() {
		String packagedSurveyUri = packagedSurvey.getUri();
		String publishedSurveyUri = publishedSurvey.getUri();
		if (! publishedSurveyUri.equals(packagedSurveyUri)) {
			throw new RuntimeException(String.format("Packaged survey uri (%s) is different from the expected one (%s)", packagedSurveyUri, publishedSurveyUri));
		}
	}

	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
	
	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public CollectSurvey getPublishedSurvey() {
		return publishedSurvey;
	}
	
	public void setPublishedSurvey(CollectSurvey publishedSurvey) {
		this.publishedSurvey = publishedSurvey;
	}
	
	public CollectSurvey getPackagedSurvey() {
		return packagedSurvey;
	}
	
	public void setPackagedSurvey(CollectSurvey packagedSurvey) {
		this.packagedSurvey = packagedSurvey;
	}
	
}
