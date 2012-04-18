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
	
	public void save(Configuration configuration) {
		this.configuration = configuration;
		configurationDao.save(configuration);
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
}
