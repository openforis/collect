package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyBackupJob.OutputFormat;
import org.openforis.collect.io.data.backup.BackupStorageManager;
import org.openforis.collect.io.proxy.SurveyBackupJobProxy;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.User;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.collect.web.session.SessionState;
import org.openforis.concurrency.proxy.JobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 * 
 */
@Controller
@Scope(SCOPE_SESSION)
@RequestMapping("api")
public class BackupRestoreController {
	
	private static final String BACKUP_FILE_EXTENSION = "collect-backup";
	
	@Autowired
	private RecordSessionManager sessionManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private BackupStorageManager backupStorageManager;
	@Autowired
	private CollectJobManager jobManager;
	
	private SurveyBackupJob backupJob;

	@RequestMapping(value = "survey/{surveyId}/backup/latest/info", method=GET)
	public @ResponseBody BackupInfo getLatestBackupInfo(@PathVariable("surveyId") int surveyId) {
		CollectSurvey survey = surveyManager.getById(surveyId);
		final Date date = backupStorageManager.getLastBackupDate(survey.getName());
		RecordFilter filter = new RecordFilter(survey);
		filter.setModifiedSince(date);
		final int updatedRecordsSinceBackupDateCount = recordManager.countRecords(filter);
		return new BackupInfo(date, updatedRecordsSinceBackupDateCount);
	}
	
	@RequestMapping(value = "survey/{surveyId}/backup/start", method=POST)
	@Transactional
	public @ResponseBody JobProxy startBackup(@PathVariable("surveyId") int surveyId) {
		CollectSurvey survey = surveyManager.getById(surveyId);
		return startFullExport(survey, true, false, null, true);
	}

	@Transactional
	public JobProxy startFullExport(CollectSurvey survey, boolean includeRecordFiles, boolean onlyOwnedRecords, String[] rootEntityKeyValues, boolean full) {
		if ( backupJob == null || ! backupJob.isRunning() ) {
			resetJobs();
			
			RecordFilter filter = createRecordFilter(survey, null, onlyOwnedRecords, rootEntityKeyValues);
			
			SurveyBackupJob job = jobManager.createJob(SurveyBackupJob.class);
			job.setFull(full);
			if (full) {
				job.setOutputFormat(OutputFormat.DESKTOP_FULL);
			} else {
				job.setOutputFormat(OutputFormat.ONLY_DATA);
			}
			job.setSurvey(survey);
			job.setIncludeData(true);
			job.setIncludeRecordFiles(includeRecordFiles);
			job.setRecordFilter(filter);
			backupJob = job;
			
			jobManager.start(job);
		}
		return getCurrentJob();
	}
	
	@RequestMapping(value="survey/{surveyId}/backup/latest.collect-backup", method=GET)
	public void downloadLatestBackup(@PathVariable("surveyId") int surveyId, HttpServletResponse response) throws FileNotFoundException, IOException {
		CollectSurvey survey = surveyManager.getById(surveyId);
		String surveyName = survey.getName();
		File file = backupStorageManager.getLastBackupFile(surveyName);
		Date date = backupStorageManager.getLastBackupDate(surveyName);
		Controllers.writeFileToResponse(response, file, 
				String.format("%s-%s.%s", surveyName, Dates.formatLocalDateTime(date), BACKUP_FILE_EXTENSION), 
				MediaTypes.ZIP_CONTENT_TYPE);
	}
	
	@RequestMapping(value="survey/{surveyId}/backup/result", method=GET)
	public void downloadBackupExportResult(HttpServletResponse response) throws FileNotFoundException, IOException {
		File file = backupJob.getOutputFile();
		CollectSurvey survey = backupJob.getSurvey();
		String surveyName = survey.getName();
		Controllers.writeFileToResponse(response, file, 
				String.format("%s-%s.%s", surveyName, Dates.formatLocalDateTime(new Date()), BACKUP_FILE_EXTENSION), 
				MediaTypes.ZIP_CONTENT_TYPE);
	}
	
	private RecordFilter createRecordFilter(CollectSurvey survey, Integer rootEntityId, boolean onlyOwnedRecords, String[] rootEntityKeyValues) {
		RecordFilter recordFilter = new RecordFilter(survey, rootEntityId);
		
		//filter by record owner
		if ( onlyOwnedRecords ) {
			SessionState sessionState = sessionManager.getSessionState();
			User user = sessionState.getUser();
			recordFilter.setOwnerId(user.getId());
		}
		
		//filter by root entity keys
		recordFilter.setKeyValues(rootEntityKeyValues);
		
		return recordFilter;
	}
	
	private void resetJobs() {
		backupJob = null;
	}
	
	public JobProxy getCurrentJob() {
		if ( backupJob != null ) {
			return new SurveyBackupJobProxy(backupJob);
		}
		return null;
	}
	
	public static class BackupInfo {
		
		private Date date;
		private int updatedRecordsSinceBackup;
		
		public BackupInfo() {
		}
		
		public BackupInfo(Date date, int updatedRecordsSinceBackup) {
			super();
			this.date = date;
			this.updatedRecordsSinceBackup = updatedRecordsSinceBackup;
		}
		
		public Date getDate() {
			return date;
		}
		
		public int getUpdatedRecordsSinceBackup() {
			return updatedRecordsSinceBackup;
		}
	}
}
