package org.openforis.collect.manager;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.persistence.ConfigurationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class ConfigurationManager {

	@Autowired
	private ConfigurationDao configurationDao;
	
	private Configuration configuration;
	
	public void init() {
		this.configuration = configurationDao.load();
	}
	
	@Transactional
	public void updateUploadPath(String uploadPath) {
		updateConfigurationPathItem(ConfigurationItem.RECORD_FILE_UPLOAD_PATH, uploadPath);
	}

	@Transactional
	public void updateIndexPath(String indexPath) {
		updateConfigurationPathItem(ConfigurationItem.RECORD_INDEX_PATH, indexPath);
	}
	
	@Transactional
	public void updateBakcupStoragePath(String indexPath) {
		updateConfigurationPathItem(ConfigurationItem.BACKUP_STORAGE_PATH, indexPath);
	}
	
	@Transactional
	private void updateConfigurationPathItem(ConfigurationItem configurationItem, String path) {
		if ( StringUtils.isNotBlank(path) ) {
			validateWritableDirectory(path);
		}
		updateConfigurationItem(configurationItem, path);
	}

	private void validateWritableDirectory(String path) {
		File dir = new File(path);
		if ( ! ( dir.exists() && dir.isDirectory() && dir.canWrite() ) ) {
			throw new IllegalArgumentException(String.format("Invalid path: %s has to be a writable directory", dir.getAbsolutePath()));
		}
	}

	@Transactional
	public void updateConfigurationItem(ConfigurationItem item, String value) {
		configuration.put(item, value);
		configurationDao.save(configuration);
	}
	
	@Transactional
	public void save(Configuration configuration) {
		configurationDao.save(configuration);
		this.configuration = configuration;
	}
	
	public Configuration getConfiguration() {
		try {
			return configuration == null ? null: (Configuration) (configuration.clone());
		} catch (CloneNotSupportedException e) {
			//it should never happen
			throw new RuntimeException("Error cloning configuration", e);
		}
	}
}
