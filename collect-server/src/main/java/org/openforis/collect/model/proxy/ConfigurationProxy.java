/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.model.Configuration.ConfigurationItem;

/**
 * @author S. Ricci
 *
 */
public class ConfigurationProxy implements Proxy {

	private transient Configuration configuration;
	private String defaultRecordFileUploadPath;
	private String defaultRecordIndexPath;
	private String defaultBackupStoragePath;

	public ConfigurationProxy(Configuration configuration, String defaultRecordFileUploadPath,
			String defaultRecordIndexPath, String defaultBackupStoragePath) {
		super();
		this.configuration = configuration;
		this.defaultRecordFileUploadPath = defaultRecordFileUploadPath;
		this.defaultRecordIndexPath = defaultRecordIndexPath;
		this.defaultBackupStoragePath = defaultBackupStoragePath;
	}

	public String getUploadPath() {
		return configuration.getUploadPath();
	}

	public String getDefaultUploadPath() {
		return defaultRecordFileUploadPath;
	}

	public String getIndexPath() {
		return configuration.getIndexPath();
	}

	public String getDefaultIndexPath() {
		return defaultRecordIndexPath;
	}

	public String getDefaultRecordFileUploadPath() {
		return defaultRecordFileUploadPath;
	}

	public String getDefaultRecordIndexPath() {
		return defaultRecordIndexPath;
	}

	public String getBackupStoragePath() {
		return configuration.getBackupStoragePath();
	}

	public String getDefaultBackupStoragePath() {
		return defaultBackupStoragePath;
	}

	public String getAllowedRestoreKey() {
		return configuration.get(ConfigurationItem.ALLOWED_RESTORE_KEY);
	}

	public String getRemoteCloneUrl() {
		return configuration.get(ConfigurationItem.REMOTE_CLONE_URL);
	}

	public String getRemoteCloneRestoreKey() {
		return configuration.get(ConfigurationItem.REMOTE_RESTORE_KEY);
	}

}
