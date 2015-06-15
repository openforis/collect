package org.openforis.collect.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.io.data.DataRestoreJob;
import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.web.controller.upload.UploadItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
public class RestoreController extends BasicController {

	//private static Log LOG = LogFactory.getLog(RestoreController.class);
	
	@Autowired
	private ConfigurationManager configurationManager;
	@Autowired
	private CollectJobManager jobManager;
	
	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/survey-data/restore.json", method = RequestMethod.POST)
	public @ResponseBody String restoreData(UploadItem uploadItem) throws IOException {
		return startRestoreJob(uploadItem);
	}
	
	@RequestMapping(value = "/survey-data/restore-remotely.json", method = RequestMethod.POST)
	public @ResponseBody String restoreDataRemotely(UploadItem uploadItem, @RequestParam String restoreKey) throws IOException {
		String allowedRestoreKey = configurationManager.getConfiguration().get(ConfigurationItem.ALLOWED_RESTORE_KEY);
		if (StringUtils.isNotBlank(allowedRestoreKey) && allowedRestoreKey.equals(restoreKey)) {
			return startRestoreJob(uploadItem);
		} else {
			throw new IllegalArgumentException("Restore not allowed: invalid restore key");
		}
	}
	
	private String startRestoreJob(UploadItem uploadItem) throws IOException,
	FileNotFoundException, ZipException {
		File tempFile = copyContentToFile(uploadItem);
		
		String surveyUri = extractSurveyUri(tempFile);
		
		DataRestoreJob job = jobManager.createJob(DataRestoreJob.class);
		job.setStoreRestoredFile(true);
		job.setSurveyUri(surveyUri);
		job.setFile(tempFile);
		job.setOverwriteAll(true);
		job.setRestoreUploadedFiles(true);
		
		String lockId = surveyUri;
		jobManager.start(job, lockId);
		
		return lockId;
	}
	
	private String extractSurveyUri(File tempFile) throws ZipException,
			IOException, FileNotFoundException {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(tempFile);
		File infoFile = backupFileExtractor.extractInfoFile();
		SurveyBackupInfo backupInfo = SurveyBackupInfo.parse(new FileInputStream(infoFile));
		String surveyUri = backupInfo.getSurveyUri();
		return surveyUri;
	}
	
	private File copyContentToFile(UploadItem uploadItem) throws IOException,
			FileNotFoundException {
		CommonsMultipartFile fileData = uploadItem.getFileData();
		InputStream is = fileData.getInputStream();
		File tempFile = File.createTempFile("collect-upload-item", ".temp");
		FileOutputStream os = new FileOutputStream(tempFile);
		IOUtils.copy(is, os);
		return tempFile;
	}
	
}
