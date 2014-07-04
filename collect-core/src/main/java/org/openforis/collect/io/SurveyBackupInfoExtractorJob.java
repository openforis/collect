/**
 * 
 */
package org.openforis.collect.io;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openforis.collect.io.internal.SurveyBackupInfoExtractorTask;
import org.openforis.collect.io.internal.SurveyBackupVerifierTask;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
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
public class SurveyBackupInfoExtractorJob extends Job {

	@Autowired
	private SurveyManager surveyManager;

	//input
	private File file;
	private boolean validate;
	
	//output
	private CollectSurvey survey;
	private SurveyBackupInfo info;

	//internal variables
	private boolean fullBackup;
	private ZipFile zipFile;
	
	@Override
	protected void initInternal() throws Throwable {
		String ext = FilenameUtils.getExtension(file.getName());
		fullBackup = ArrayUtils.contains(SurveyRestoreJob.COMPLETE_BACKUP_FILE_EXTENSIONS, ext);
		if ( fullBackup ) {
			this.zipFile = new ZipFile(file);
		}
		super.initInternal();
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		if ( fullBackup ) {
			addTask(SurveyBackupVerifierTask.class);
			addTask(SurveyBackupInfoExtractorTask.class);
		}
		addTask(IdmlUnmarshallTask.class);
	}
	
	@Override
	protected void prepareTask(Task task) {
		if ( task instanceof SurveyBackupVerifierTask ) {
			SurveyBackupVerifierTask t = (SurveyBackupVerifierTask) task;
			t.setZipFile(zipFile);
		} else if ( task instanceof SurveyBackupInfoExtractorTask ) {
			BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
			SurveyBackupInfoExtractorTask t = (SurveyBackupInfoExtractorTask) task;
			File infoFile = backupFileExtractor.extractInfoFile();
			t.setFile(infoFile);
		} else if ( task instanceof IdmlUnmarshallTask ) {
			File idmlFile;
			if ( zipFile == null ) {
				idmlFile = file;
			} else {
				BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
				idmlFile = zipFile == null ? file: backupFileExtractor.extractIdmlFile();
			}
			IdmlUnmarshallTask t = (IdmlUnmarshallTask) task;
			t.setSurveyManager(surveyManager);
			t.setValidate(validate);
			t.setFile(idmlFile);
		}
	}
	
	@Override
	protected void onTaskCompleted(Task task) {
		super.onTaskCompleted(task);
		if ( task instanceof SurveyBackupInfoExtractorTask ) {
			this.info = ((SurveyBackupInfoExtractorTask) task).getInfo();
		} else if ( task instanceof IdmlUnmarshallTask ) {
			this.survey = ((IdmlUnmarshallTask) task).getSurvey();
			if ( zipFile == null ) {
				//extracting backup info from XML file
				this.info = SurveyBackupInfo.createOldVersionInstance(survey.getUri());
			}
		}
	}
	
	@Override
	protected void onEnd() {
		super.onEnd();
		if ( zipFile != null ) {
			try {
				zipFile.close();
			} catch (IOException e) {
				log().warn("Error closing zip file", e);
			}
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
	
	public boolean isValidate() {
		return validate;
	}
	
	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	
	public SurveyBackupInfo getInfo() {
		return info;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}
	
}
