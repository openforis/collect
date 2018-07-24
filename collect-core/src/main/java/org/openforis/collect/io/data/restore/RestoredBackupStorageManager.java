package org.openforis.collect.io.data.restore;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.BaseStorageManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.utils.Dates;
import org.springframework.stereotype.Component;

@Component
public class RestoredBackupStorageManager extends BaseStorageManager {
	
	private static final long serialVersionUID = 1L;
	
	protected static final Logger LOG = LogManager.getLogger(RestoredBackupStorageManager.class);
	
	private static final String DEFAULT_STORAGE_SUBFOLDER = "restore";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd_HH-mm-ss-SSSZ";
	private static final String TEMP_DIRECTORY_NAME = "unsuccessfull";
	
	public RestoredBackupStorageManager() {
		super(DEFAULT_STORAGE_SUBFOLDER);
	}
	
	@PostConstruct
	public void init() {
		initStorageDirectory();
	}

	protected void initStorageDirectory() {
		super.initStorageDirectory(ConfigurationItem.RESTORED_BACKUP_STORAGE_PATH);
	}

	public File storeTemporaryFile(String surveyName, File file) {
		try {
			File tempDirectory = getOrCreateTempDirectory(surveyName);
			String tempFileName = createTempFileName(surveyName);
			File tempFile = new File(tempDirectory, tempFileName);
			if (tempFile.createNewFile()) {
				FileUtils.copyFile(file, tempFile);
			} else {
				throw new RuntimeException("Cannot create file or file already exists: " + tempFile.getAbsolutePath());
			}
			return tempFile;
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	private String createTempFileName(String surveyName) {
		String formattedDate = Dates.format(new Date(), DATE_TIME_FORMAT);
		return String.format("%s_%s.collect-backup", surveyName, formattedDate);
	}

	public void moveToFinalFolder(String surveyName, File tempFile) {
		File surveyDirectory = getOrCreateFinalDirectory(surveyName);
		try {
			FileUtils.moveFileToDirectory(tempFile, surveyDirectory, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File getOrCreateFinalDirectory(String surveyName) {
		return new File(storageDirectory, surveyName);
	}

	private File getOrCreateTempDirectory(String surveyName) {
		File surveyDirectory = getOrCreateFinalDirectory(surveyName);
		File tempDirectory = new File(surveyDirectory, TEMP_DIRECTORY_NAME);
		tempDirectory.mkdirs();
		return tempDirectory;
	}
	
}
