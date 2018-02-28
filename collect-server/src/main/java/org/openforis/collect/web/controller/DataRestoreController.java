package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.io.data.DataRestoreJob;
import org.openforis.collect.io.data.DataRestoreTask.OverwriteStrategy;
import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserRoles;
import org.openforis.collect.web.controller.upload.UploadItem;
import org.openforis.commons.web.JobStatusResponse;
import org.openforis.concurrency.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("api")
@Scope(SCOPE_SESSION)
public class DataRestoreController extends BasicController {

	//private static Log LOG = LogFactory.getLog(RestoreController.class);
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private ConfigurationManager configurationManager;
	@Autowired
	private CollectJobManager jobManager;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserGroupManager userGroupManager;
	
	@Secured({UserRoles.ENTRY})
	@RequestMapping(value = "/surveys/restore/data", method=POST, consumes=MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JobStatusResponse restoreData(@RequestParam("file") MultipartFile multipartFile, 
			@RequestParam(required=false) String surveyName,
			@RequestParam(required=false, defaultValue="true") boolean validateRecords,
			@RequestParam(required=false, defaultValue="false") boolean deleteAllRecordsBeforeImport,
			@RequestParam(required=false, defaultValue="OVERWRITE_OLDER") String recordOverwriteStrategy) throws IOException {
		User loggedUser = sessionManager.getLoggedUser();
		try {
			DataRestoreJob job = startRestoreJob(multipartFile.getInputStream(), surveyName == null, surveyName, loggedUser,
					validateRecords, deleteAllRecordsBeforeImport, OverwriteStrategy.valueOf(recordOverwriteStrategy));
			return createResponse(job);
		} catch (Exception e) {
			JobStatusResponse response = new JobStatusResponse();
			response.setErrorStatus();
			response.setErrorMessage(e.getMessage());
			return response;
		}
	}
	
	@RequestMapping(value = "/surveys/{surveyName}/data/restoreremotely.json", method=POST)
	public @ResponseBody RemoteDataRestoreResponse restoreDataRemotely(UploadItem uploadItem, 
			@PathVariable String surveyName, 
			@RequestParam String restoreKey) {
		RemoteDataRestoreResponse response = new RemoteDataRestoreResponse();
		String allowedRestoreKey = configurationManager.getConfiguration().get(ConfigurationItem.ALLOWED_RESTORE_KEY);
		if (StringUtils.isBlank(allowedRestoreKey) || allowedRestoreKey.equals(restoreKey)) {
			try {
				User user = userManager.loadAdminUser();
				boolean newSurvey = surveyManager.get(surveyName) == null;
				DataRestoreJob job = startRestoreJob(uploadItem.getFileData().getInputStream(), newSurvey, surveyName, user,
						true, false, OverwriteStrategy.OVERWRITE_OLDER);
				response.setJobId(job.getId().toString());
			} catch (Exception e) {
				response.setErrorStatus();
				response.setErrorMessage(e.getMessage());
			}
		} else {
			response.setErrorStatus();
			response.setErrorMessage("Restore not allowed: invalid restore key");
		}
		return response;
	}
	
	@Secured({UserRoles.ENTRY})
	@RequestMapping(value = "/surveys/data/restorejobs/{jobId}/status.json", method=GET)
	public @ResponseBody RemoteDataRestoreResponse getRestoreDataRemotelyStatus(@PathVariable String jobId) throws IOException {
		RemoteDataRestoreResponse response;
		Job job = jobManager.getJob(jobId);
		if (job == null || ! (job instanceof DataRestoreJob)) {
			response = new RemoteDataRestoreResponse();
			response.setErrorStatus();
			response.setErrorMessage("Job not found");
		} else {
			response = createRemoteDataRestoreResponse(job);
		}
		return response;
	}

	@RequestMapping(value = "/surveys/data/restore/jobs/{jobId}/abort.json", method=GET)
	public @ResponseBody RemoteDataRestoreResponse abortRestoreDataRemotelyJob(@PathVariable String jobId) throws IOException {
		RemoteDataRestoreResponse response;
		Job job = jobManager.getJob(jobId);
		if (job == null || ! (job instanceof DataRestoreJob)) {
			response = new RemoteDataRestoreResponse();
			response.setErrorStatus();
			response.setErrorMessage("Job not found");
		} else {
			job.abort();
			response = createRemoteDataRestoreResponse(job);
		}
		return response;
	}
	
	private JobStatusResponse createResponse(Job job) {
		JobStatusResponse response = new JobStatusResponse();
		fillResponse(response, job);
		return response;
	}
	
	private RemoteDataRestoreResponse createRemoteDataRestoreResponse(Job job) {
		RemoteDataRestoreResponse response = new RemoteDataRestoreResponse();
		fillResponse(response, job);
		return response;
	}

	private void fillResponse(JobStatusResponse response, Job job) {
		response.setJobId(job.getId().toString());
		response.setJobStatus(job.getStatus());
		response.setJobProgress(job.getProgressPercent());
		response.setErrorMessage(job.getErrorMessage());
	}
	
	private DataRestoreJob startRestoreJob(InputStream fileInputStream, boolean newSurvey, 
			String expectedSurveyName, User user, boolean validateRecords, boolean deleteAllRecords,
			OverwriteStrategy recordOverwriteStrategy) throws IOException,	FileNotFoundException, ZipException {
		File tempFile = File.createTempFile("ofc_data_restore", ".collect-backup");
		FileUtils.copyInputStreamToFile(fileInputStream, tempFile);
		
		SurveyBackupInfo info = extractInfo(tempFile);
		
		CollectSurvey publishedSurvey = findPublishedSurvey(info);
		if (newSurvey) {
			checkPackagedNewSurveyValidity(info);
		} else {
			checkPackagedSurveyValidity(info, expectedSurveyName);
		}
		UserGroup newSurveyUserGroup = userGroupManager.getDefaultPublicUserGroup();
		
		DataRestoreJob job = jobManager.createJob(DataRestoreJob.JOB_NAME, DataRestoreJob.class);
		job.setUser(user);
		job.setStoreRestoredFile(true);
		job.setPublishedSurvey(publishedSurvey);
		job.setNewSurveyUserGroup(newSurveyUserGroup);
		job.setFile(tempFile);
		job.setRecordOverwriteStrategy(recordOverwriteStrategy);
		job.setRestoreUploadedFiles(true);
		job.setValidateRecords(validateRecords);
		job.setDeleteAllRecordsBeforeRestore(deleteAllRecords);
		
		String lockId = extractSurveyUri(tempFile);
		jobManager.start(job, lockId);
		
		return job;
	}

	private void checkPackagedSurveyValidity(SurveyBackupInfo info,
			String expectedSurveyName) throws ZipException, FileNotFoundException, IOException {
		CollectSurvey publishedSurvey = findPublishedSurvey(info);
		if (publishedSurvey == null) {
			throw new IllegalArgumentException(String.format("Published survey not found (URI=\"%s\")", info.getSurveyUri()));
		}
	}

	private CollectSurvey findPublishedSurvey(SurveyBackupInfo info) throws ZipException, IOException {
		CollectSurvey survey = surveyManager.getByUri(info.getSurveyUri());
		return survey;
	}

	private SurveyBackupInfo extractInfo(File tempFile) throws ZipException,
			IOException {
		BackupFileExtractor backupFileExtractor = null;
		try {
			backupFileExtractor = new BackupFileExtractor(tempFile);
			SurveyBackupInfo info = backupFileExtractor.extractInfo();
			return info;
		} finally {
			IOUtils.closeQuietly(backupFileExtractor);
		}
	}

	private void checkPackagedNewSurveyValidity(SurveyBackupInfo info) throws ZipException, IOException {
		CollectSurvey publishedSurvey = findPublishedSurvey(info);
		if (publishedSurvey != null) {
			throw new IllegalArgumentException("The backup file is associated to a survey with name " + publishedSurvey.getName());
		}
	}

	private String extractSurveyUri(File tempFile) throws ZipException,
			IOException, FileNotFoundException {
		BackupFileExtractor backupFileExtractor = null;
		try {
			backupFileExtractor = new BackupFileExtractor(tempFile);
			File infoFile = backupFileExtractor.extractInfoFile();
			SurveyBackupInfo backupInfo = SurveyBackupInfo.parse(new FileInputStream(infoFile));
			String surveyUri = backupInfo.getSurveyUri();
			return surveyUri;
		} finally {
			IOUtils.closeQuietly(backupFileExtractor);
		}
	}
	
	public static class RemoteDataRestoreResponse extends JobStatusResponse {
		
	}
	
}
