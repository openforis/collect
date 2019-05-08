package org.openforis.collect.reporting;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openforis.collect.manager.BaseStorageManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.utils.Dates;
import org.openforis.commons.io.OpenForisIOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class MondrianSchemaStorageManager extends BaseStorageManager implements InitializingBean {
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_STORAGE_SUBFOLDER = "rdb" + File.separator + "mondrian";
	
	public MondrianSchemaStorageManager() {
		super(DEFAULT_STORAGE_SUBFOLDER);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	public void init() {
		initStorageDirectory();
	}

	protected void initStorageDirectory() {
		super.initStorageDirectory(ConfigurationItem.MONDRIAN_SCHEMA_PATH);
	}

	public boolean existsSchemaFile(String surveyName) {
		File file = getSchemaFile(surveyName);
		return file.exists() && file.length() > 0;
	}

	public boolean createBackupCopy(String surveyName) throws IOException {
		File file = getSchemaFile(surveyName);
		if (file.exists() && file.length() > 0) {
			String newBackupFileName = String.format("%s.%s", getSchemaFileName(surveyName), Dates.formatDate(new Date()));
			File backupFile = new File(getOrCreateBackupFolder(), newBackupFileName);
			FileUtils.copyFile(file, backupFile);
			return true;
		} else {
			return false;
		}
	}

	private File getOrCreateBackupFolder() {
		File parentDir = new File(storageDirectory, "backup");
		parentDir.mkdirs();
		return parentDir;
	}

	public File getSchemaFile(String surveyName) {
		return new File(storageDirectory, getSchemaFileName(surveyName));
	}
	
	public String readSchemaFile(String surveyName) {
		File schemaFile = getSchemaFile(surveyName);
		try {
			String xml = FileUtils.readFileToString(schemaFile, OpenForisIOUtils.UTF_8);
			return xml;
		} catch (IOException e) {
			throw new RuntimeException("Error reading schema file for survey: " + surveyName, e);
		}
	}
	
	public boolean deleteSchemaFile(String surveyName) {
		File file = getSchemaFile(surveyName);
		return file.delete();
	}

	private String getSchemaFileName(String surveyName) {
		return String.format("%s.xml", surveyName);
	}

}
