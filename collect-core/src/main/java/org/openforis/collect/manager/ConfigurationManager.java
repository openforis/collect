package org.openforis.collect.manager;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.persistence.ConfigurationDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
public class ConfigurationManager {

	@Autowired
	private ConfigurationDao configurationDao;
	
	private Configuration configuration;
	
	public void init() {
		configuration = configurationDao.load();
	}
	
	public void updateUploadPath(String uploadPath) {
		if ( StringUtils.isNotBlank(uploadPath) ) {
			validateWritableDirectory(uploadPath);
		}
		configuration.put(Configuration.UPLOAD_PATH_KEY, uploadPath);
		configurationDao.save(configuration);
	}

	public void updateIndexPath(String indexPath) {
		if ( StringUtils.isNotBlank(indexPath) ) {
			validateWritableDirectory(indexPath);
		}
		configuration.put(Configuration.INDEX_PATH_KEY, indexPath);
		configurationDao.save(configuration);
	}

	private void validateWritableDirectory(String path) {
		File dir = new File(path);
		if ( ! ( dir.exists() && dir.isDirectory() && dir.canWrite() ) ) {
			throw new IllegalArgumentException("Invalid path: it has to be a writable directory");
		}
	}

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
