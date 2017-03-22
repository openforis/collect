package org.openforis.collect.manager;

import java.io.File;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import static org.openforis.collect.utils.Files.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BaseStorageManager implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Log LOG = LogFactory.getLog(BaseStorageManager.class);
	
	protected static final String DATA_FOLDER_NAME = "data";

	private static final String OPENFORIS_FOLDER_NAME = "OpenForis";
	private static final String COLLECT_FOLDER_NAME = "Collect";

	private static final String DATA_SUBDIR = OPENFORIS_FOLDER_NAME + File.separator + COLLECT_FOLDER_NAME + File.separator + DATA_FOLDER_NAME;
	
	private static final String USER_HOME = "user.home";

	private static final File USER_HOME_DATA_FOLDER = getReadableSysPropLocation(USER_HOME, DATA_SUBDIR);
	
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
		this(null, null);
	}
	
	public BaseStorageManager(String defaultSubFolder) {
		this(null, defaultSubFolder);
	}
	
	public BaseStorageManager(String defaultRootStoragePath, String defaultSubFolder) {
		this.defaultRootStoragePath = defaultRootStoragePath;
		this.defaultSubFolder = defaultSubFolder;
	}
	
	protected void initStorageDirectory(ConfigurationItem configurationItem) {
		initStorageDirectory(configurationItem, true);
	}
	
	protected boolean initStorageDirectory(ConfigurationItem configurationItem, boolean createIfNotExists) {
		Configuration configuration = configurationManager.getConfiguration();
		
		String customStoragePath = configuration.get(configurationItem);
		
		if ( StringUtils.isBlank(customStoragePath) ) {
			storageDirectory = getDefaultStorageDirectory();
		} else {
			storageDirectory = new File(customStoragePath);
		}
		boolean result = storageDirectory.exists();
		if (! result) {
			if (createIfNotExists) {
				result = storageDirectory.mkdirs();
			}
		}
		
		if (LOG.isInfoEnabled() ) {
			if (result) {
				LOG.info(String.format("Using %s directory: %s", configurationItem.getLabel(), storageDirectory.getAbsolutePath()));
			} else {
				LOG.info(String.format("%s directory %s does not exist or it's not accessible", configurationItem.getLabel(), storageDirectory.getAbsolutePath()));
			}
		}
		return result;
	}

	protected File getDefaultStorageRootDirectory() {
		if ( defaultRootStoragePath == null ) {
			//try to use user home data folder
			File result = USER_HOME_DATA_FOLDER;
			if ( result == null ) {
				//try to use data folder in catalina.base path
				result = getReadableSysPropLocation("catalina.base", DATA_FOLDER_NAME);
				if (result == null) {
					//try to use collect.root system property
					String rootPath = System.getProperty("collect.root");
					if (rootPath != null) {
						File webappsFolder = new File(rootPath).getParentFile();
						File baseFolder = webappsFolder.getParentFile();
						result = new File(baseFolder, DATA_FOLDER_NAME);
					}
				}
			}
			if (result == null || ! result.exists()) {
				result = TEMP_FOLDER;
			}
			return result;
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
	
	protected void setDefaultSubFolder(String defaultSubFolder) {
		this.defaultSubFolder = defaultSubFolder;
	}
}
