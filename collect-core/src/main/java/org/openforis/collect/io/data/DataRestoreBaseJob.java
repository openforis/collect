/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.File;
import java.util.zip.ZipFile;

import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.io.SurveyRestoreJob;
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
	protected transient CollectSurvey publishedSurvey; //optional: if not specified, the packaged survey will be published as a new one
	protected transient CollectSurvey packagedSurvey; //optional: if not specified, it will be extracted from the ZIP file
	
	//temporary instance variables
	protected transient boolean newSurvey;
	protected transient String surveyName; //published survey name or packaged survey name
	protected transient ZipFile zipFile;
	protected transient BackupFileExtractor backupFileExtractor;
	protected transient boolean oldBackupFormat;

	@Override
	public void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		newSurvey = publishedSurvey == null;
		zipFile = new ZipFile(file);
		backupFileExtractor = new BackupFileExtractor(zipFile);
		oldBackupFormat = backupFileExtractor.isOldFormat();
		surveyName = newSurvey ? extractSurveyName() : publishedSurvey.getName();
	}
	
	@Override
	protected void validateInput() throws Throwable {
		super.validateInput();
		boolean newSurvey = publishedSurvey == null;
		SurveyBackupInfo backupInfo = new BackupFileExtractor(file).extractInfo();
		CollectSurvey existingPublishedSurvey = findExistingPublishedSurvey(backupInfo);
		if (newSurvey) {
			if (existingPublishedSurvey != null) {
				throw new IllegalArgumentException(String.format("The backup file is associated to an already published survey: %s", existingPublishedSurvey.getName()));
			}
		} else {
			String publishedSurveyUri = publishedSurvey.getUri();
			String packagedSurveyUri = backupInfo.getSurveyUri();
			if (! publishedSurveyUri.equals(packagedSurveyUri)) {
				throw new RuntimeException(String.format("Packaged survey uri (%s) is different from the expected one (%s)", packagedSurveyUri, publishedSurveyUri));
			}
		}
	}

	@Override
	protected void buildTasks() throws Throwable {
		if (newSurvey) {
			addTask(SurveyRestoreJob.class);
		} else if (packagedSurvey == null) {
			addTask(createTask(IdmlUnmarshallTask.class));
		}
	}

	@Override
	protected void initializeTask(Worker task) {
		if (task instanceof SurveyRestoreJob) {
			SurveyRestoreJob t = (SurveyRestoreJob) task;
			t.setFile(file);
			t.setRestoreIntoPublishedSurvey(true);
			t.setSurveyName(surveyName);
			t.setValidateSurvey(true);
		} else if ( task instanceof IdmlUnmarshallTask ) {
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
		if (task instanceof SurveyRestoreJob) {
			publishedSurvey = ((SurveyRestoreJob) task).getSurvey();
			packagedSurvey = publishedSurvey;
		} else if ( task instanceof IdmlUnmarshallTask ) {
			CollectSurvey survey = ((IdmlUnmarshallTask) task).getSurvey();
			if ( survey == null ) {
				throw new RuntimeException("Error extracting packaged survey");
			} else {
				packagedSurvey = survey;
			}
			this.packagedSurvey = survey;
		}
	}

	private CollectSurvey findExistingPublishedSurvey(SurveyBackupInfo backupInfo) {
		CollectSurvey existingPublishedSurvey = surveyManager.get(backupInfo.getSurveyName());
		if (existingPublishedSurvey == null) {
			existingPublishedSurvey = surveyManager.getByUri(backupInfo.getSurveyUri());
		}
		return existingPublishedSurvey;
	}

	private String extractSurveyName() {
		SurveyBackupInfo info = backupFileExtractor.extractInfo();
		return info.getSurveyName();
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
