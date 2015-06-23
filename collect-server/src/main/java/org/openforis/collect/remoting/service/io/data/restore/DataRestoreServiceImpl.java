package org.openforis.collect.remoting.service.io.data.restore;

import java.io.File;
import java.io.FileInputStream;

import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.io.data.DataRestoreJob;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataRestoreServiceImpl implements DataRestoreService {

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CollectJobManager jobManager;
	
	@Override
	public String startSurveyDataRestore(String surveyName, File backupFile) {
		CollectSurvey survey = surveyManager.get(surveyName);
		
		String surveyUri = extractSurveyUri(backupFile);
		checkValidSurvey(surveyName, surveyUri);
		
		DataRestoreJob job = jobManager.createJob(DataRestoreJob.class);
		job.setStoreRestoredFile(true);
		job.setPublishedSurvey(survey);
		job.setFile(backupFile);
		job.setOverwriteAll(true);
		job.setRestoreUploadedFiles(true);
		
		String lockId = surveyUri;
		jobManager.start(job, lockId);
		
		return lockId;
	}

	private void checkValidSurvey(String surveyName, String surveyUri) {
		CollectSurvey expectedSurvey = surveyManager.get(surveyName);
		String expectedSurveyUri = expectedSurvey.getUri();
		if (! surveyUri.equals(expectedSurveyUri)) {
			throw new IllegalArgumentException("The backup file is not related to the specified survey");
		}
	}

	private String extractSurveyUri(File tempFile) {
		try {
			BackupFileExtractor backupFileExtractor = new BackupFileExtractor(tempFile);
			File infoFile = backupFileExtractor.extractInfoFile();
			SurveyBackupInfo backupInfo = SurveyBackupInfo.parse(new FileInputStream(infoFile));
			String surveyUri = backupInfo.getSurveyUri();
			return surveyUri;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
