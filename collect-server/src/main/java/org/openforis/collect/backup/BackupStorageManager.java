package org.openforis.collect.backup;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.BaseStorageManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
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

	public void store(File file) {
		try {
			FileUtils.copyFileToDirectory(file, storageDirectory);
		} catch (IOException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public void getLastBackupDate(String surveyName) {
		File[] backupFiles = storageDirectory.listFiles();
		for (File backupFile : backupFiles) {
			String baseName = FilenameUtils.getBaseName(backupFile.getName());
			
		}
	}
	

}
