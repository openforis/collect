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
			String date = Dates.formatDateTime(new Date());
			String fileName = surveyName + "_" + date + ".collect-backup";
			File newFile = new File(storageDirectory, fileName);
			FileUtils.copyFile(file, newFile);
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
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
				Date date = Dates.parseDateTime(dateStr);
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
