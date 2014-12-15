/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.File;
import java.util.zip.ZipFile;

import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public abstract class DataRestoreBaseJob extends Job {

	@Autowired
	protected transient SurveyManager surveyManager;
	
	//input
	protected transient File file;
	protected transient String surveyUri;
	protected transient CollectSurvey packagedSurvey;
	protected transient CollectSurvey publishedSurvey;
	
	//temporary instance variables
	protected transient ZipFile zipFile;

	@Override
	protected void buildTasks() throws Throwable {
		if ( packagedSurvey == null ) {
			addIdmlUnmarshallTask();
		}
	}

	@Override
	public void initInternal() throws Throwable {
		zipFile = new ZipFile(file);
		if ( packagedSurvey != null ) {
			checkPackagedSurveyUri();
			surveyUri = packagedSurvey.getUri();
			initExistingSurvey();
		}
		super.initInternal();
	}

	private void addIdmlUnmarshallTask() {
		final IdmlUnmarshallTask task = createTask(IdmlUnmarshallTask.class);
		addTask(task);
	}
	
	@Override
	protected void prepareTask(Task task) {
		if ( task instanceof IdmlUnmarshallTask ) {
			IdmlUnmarshallTask t = (IdmlUnmarshallTask) task;
			BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
			File idmlFile = backupFileExtractor.extractIdmlFile();
			t.setSurveyManager(surveyManager);
			t.setFile(idmlFile);
			t.setValidate(false);
		}
		super.prepareTask(task);
	}

	protected void checkPackagedSurveyUri() {
		String packagedSurveyUri = packagedSurvey.getUri();
		if ( surveyUri != null && ! surveyUri.equals(packagedSurveyUri) ) {
			throw new RuntimeException(String.format("Packaged survey uri (%s) is different from the expected one (%s)", packagedSurveyUri, surveyUri));
		}
	}

	protected void initExistingSurvey() {
		publishedSurvey = surveyManager.getByUri(surveyUri);
		if ( publishedSurvey == null ) {
			throw new RuntimeException(String.format("Published survey with uri %s not found", surveyUri));
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
	
	public String getSurveyUri() {
		return surveyUri;
	}
	
	public void setSurveyUri(String surveyUri) {
		this.surveyUri = surveyUri;
	}
	
}
