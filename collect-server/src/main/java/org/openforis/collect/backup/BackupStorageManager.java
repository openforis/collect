package org.openforis.collect.backup;

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
public class BackupStorageManager extends BaseStorageManager {
	
	private static final long serialVersionUID = 1L;
	
	protected static Log LOG = LogFactory.getLog(BackupStorageManager.class);
	
	private static final String DEFAULT_BACKUP_STORAGE_SUBFOLDER = "backup";
	
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd_HH-mm-ss";
	
	public BackupStorageManager() {
		super(DEFAULT_BACKUP_STORAGE_SUBFOLDER);
	}
	
	@PostConstruct
	public void init() {
		initStorageDirectory();
	}

	protected void initStorageDirectory() {
		super.initStorageDirectory(ConfigurationItem.BACKUP_STORAGE_PATH);
		if ( storageDirectory == null ) {
			String message = "Survey backup directory not configured properly";
			LOG.error(message);
			throw new IllegalStateException(message);
		} else if ( LOG.isInfoEnabled() ) {
			LOG.info("Using survey backup storage directory: " + storageDirectory.getAbsolutePath());
		}
	}

	public void store(String surveyName, File file) {
		try {
			File directory = new File(storageDirectory, surveyName);
			directory.mkdir();
			String fileName = createNewBackupFileName(surveyName);
			File newFile = new File(directory, fileName);
			if (newFile.createNewFile()) {
				FileUtils.copyFile(file, newFile);
			} else {
				throw new RuntimeException("Cannot create file or file already exists: " + newFile.getAbsolutePath());
			}
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	private String createNewBackupFileName(String surveyName) {
		String date = Dates.format(new Date(), DATE_TIME_FORMAT);
		String fileName = surveyName + "_" + date + ".collect-backup";
		return fileName;
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
