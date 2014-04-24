package org.openforis.collect.manager;

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
	
	public ConfigurationManager() {
	}
	
	public void init() {
		configuration = configurationDao.load();
	}
	
	public void updateUploadPath(String uploadPath) {
		configuration.put(Configuration.UPLOAD_PATH_KEY, uploadPath);
		configurationDao.save(configuration);
	}

	public void updateIndexPath(String indexPath) {
		configuration.put(Configuration.INDEX_PATH_KEY, indexPath);
		configurationDao.save(configuration);
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
