package org.openforis.collect.manager;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BaseStorageManager implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_BASE_DATA_SUBDIR = "data";
	private static final String DEFAULT_BASE_TEMP_SUBDIR = "temp";

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	private static final String CATALINA_BASE = "catalina.base";

	@Autowired
	protected transient ConfigurationManager configurationManager;

	/**
	 * Directory in which files will be stored
	 */
	protected File storageDirectory;
	
	/**
	 * Default path in which files will be stored.
	 * If not specified, java temp folder or catalina base temp folder
	 * will be used as storage directory.
	 */
	private String defaultRootStoragePath;
	
	/**
	 * Default subfolder used together with the default storage path to determine the default storage folder
	 */
	private String defaultSubFolder;
	
	public BaseStorageManager() {
		defaultRootStoragePath = null;
		storageDirectory = null;
	}
	
	public BaseStorageManager(String defaultSubFolder) {
		this.defaultSubFolder = defaultSubFolder;
	}
	
	protected File getTempFolder() {
		return getReadableSysPropLocation(JAVA_IO_TMPDIR, null);
	}
	
	protected File getCatalinaBaseDataFolder() {
		return getReadableSysPropLocation(CATALINA_BASE, DEFAULT_BASE_DATA_SUBDIR);
	}
	
	protected File getCatalinaBaseTempFolder() {
		return getReadableSysPropLocation(CATALINA_BASE, DEFAULT_BASE_TEMP_SUBDIR);
	}

	protected File getReadableSysPropLocation(String sysProp, String subDir) {
		String base = System.getProperty(sysProp);
		if ( base != null ) {
			String path = base;
			if ( subDir != null ) {
				path = path + File.separator + subDir;
			}
			return getLocationIfAccessible(path);
		} else {
			return null;
		}
	}

	protected File getLocationIfAccessible(String path) {
		File result = new File(path);
		if ( (result.exists() || result.mkdirs()) && result.canWrite() ) {
			return result;
		} else {
			return null;
		}
	}
	
	protected void initStorageDirectory(ConfigurationItem configurationItem) {
		Configuration configuration = configurationManager.getConfiguration();
		
		String customStoragePath = configuration.get(configurationItem);
		
		if ( StringUtils.isBlank(customStoragePath) ) {
			storageDirectory = getDefaultStorageDirectory();
		} else {
			storageDirectory = new File(customStoragePath);
		}
		storageDirectory.mkdirs();
	}

	protected File getDefaultStorageRootDirectory() {
		if ( defaultRootStoragePath == null ) {
			File rootDir = getCatalinaBaseDataFolder();
			if ( rootDir == null ) {
				rootDir = getTempFolder();
			}
			return rootDir;
		} else {
			return new File(defaultRootStoragePath );
		}
	}
	
	public String getDefaultRootStoragePath() {
		return defaultRootStoragePath;
	}
	
	public void setDefaultRootStoragePath(String defaultStoragePath) {
		this.defaultRootStoragePath = defaultStoragePath;
	}
	
	public File getDefaultStorageDirectory() {
		File rootDir = getDefaultStorageRootDirectory();
		if ( rootDir == null ) {
			return null;
		} else if ( defaultSubFolder == null ) {
			return rootDir;
		} else {
			return new File(rootDir, defaultSubFolder);
		}
	}
}
