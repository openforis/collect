package org.openforis.collect.io.data.restore;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.BaseStorageManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.utils.Dates;
import org.springframework.stereotype.Component;

@Component
public class RestoredBackupStorageManager extends BaseStorageManager {
	
	private static final long serialVersionUID = 1L;
	
	protected static Log LOG = LogFactory.getLog(RestoredBackupStorageManager.class);
	
	private static final String DEFAULT_STORAGE_SUBFOLDER = "restore";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
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
		if ( storageDirectory == null ) {
			String message = "Restored backup directory not configured properly";
			LOG.error(message);
			throw new IllegalStateException(message);
		} else if ( LOG.isInfoEnabled() ) {
			LOG.info("Using restored backup storage directory: " + storageDirectory.getAbsolutePath());
		}
	}

	public File storeTemporaryFile(String surveyName, File file) {
		try {
			File tempDirectory = getOrCreateTempDirectory(surveyName);
			File tempFile = new File(tempDirectory, file.getName());
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
	
	public Date getLastBackupDate(final String surveyName) {
		FilenameFilter filenameFilter = new FilenameFilter() {
			@Override
			public boolean accept(File file, String fileName) {
				return StringUtils.startsWith(fileName, surveyName + "_");
			}
		};
		File[] backupFiles = storageDirectory.listFiles(filenameFilter);
		sortByName(backupFiles);
		for (File backupFile : backupFiles) {
			String baseName = FilenameUtils.getBaseName(backupFile.getName());
			try {
				String dateStr = baseName.substring(surveyName.length() + 1);
				Date date = Dates.parse(dateStr, DATE_TIME_FORMAT);
				return date;
			} catch (Exception e) {}
		}
		return null;
	}

	private void sortByName(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
	}
	

}
