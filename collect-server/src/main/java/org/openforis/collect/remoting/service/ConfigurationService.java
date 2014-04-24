/**
 * 
 */
package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.model.proxy.ConfigurationProxy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
public class ConfigurationService {

	@Autowired
	private ConfigurationManager configurationManager;

	public ConfigurationProxy loadConfiguration() {
		return new ConfigurationProxy(configurationManager.getConfiguration());
	}
	
	public void updateUploadPath(String uploadPath) {
		configurationManager.updateUploadPath(uploadPath);
	}
	
	public void updateIndexPath(String indexPath) {
		configurationManager.updateIndexPath(indexPath);
	}
	
}
