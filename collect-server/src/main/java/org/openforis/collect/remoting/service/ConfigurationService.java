/**
 * 
 */
package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordIndexException;
import org.openforis.collect.manager.RecordIndexManager;
import org.openforis.collect.model.proxy.ConfigurationProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author S. Ricci
 */
public class ConfigurationService {

	@Autowired
	private ConfigurationManager configurationManager;
	@Autowired
	private RecordFileManager recordFileManager;
	@Autowired
	@Qualifier("persistedRecordIndexManager")
	private RecordIndexManager recordIndexManager;
	
	//transient instance variables
	private transient String defaultRecordFileUploadPath;
	private transient String defaultRecordIndexPath;

	public void init() {
		defaultRecordFileUploadPath = recordFileManager.getDefaultStorageDirectory().getAbsolutePath();
		defaultRecordIndexPath = recordIndexManager.getDefaultStorageDirectory().getAbsolutePath();
	}
	
	public ConfigurationProxy loadConfiguration() {
		return new ConfigurationProxy(configurationManager.getConfiguration(), 
				defaultRecordFileUploadPath,
				defaultRecordIndexPath);
	}
	
	public void updateUploadPath(String uploadPath) {
		configurationManager.updateUploadPath(uploadPath);
		recordFileManager.init();
	}
	
	public void updateIndexPath(String indexPath) throws RecordIndexException {
		configurationManager.updateIndexPath(indexPath);
		boolean initialized = recordIndexManager.init();
		if ( ! initialized ) {
			throw new RuntimeException("Error initializing index path");
		}
	}
	
}
