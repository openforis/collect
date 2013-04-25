package org.openforis.collect.manager;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BaseStorageManager implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_BASE_TEMP_SUBDIR = "temp";

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	private static final String CATALINA_BASE = "catalina.base";

	@Autowired
	protected transient ConfigurationManager configurationManager;

	protected File storageDirectory;
	
	protected File getTempFolder() {
		return getReadableSysPropLocation(JAVA_IO_TMPDIR, null);
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
	
	protected void initStorageDirectory(String pathConfigurationKey, String subFolder) {
		Configuration configuration = configurationManager.getConfiguration();
		String storagePath = configuration.get(pathConfigurationKey);
		if ( StringUtils.isBlank(storagePath) ) {
			File rootDir = getCatalinaBaseTempFolder();
			if ( rootDir == null ) {
				rootDir = getTempFolder();
			}
			if ( rootDir == null ) {
				storageDirectory = null;
			} else {
				storageDirectory = new File(rootDir, subFolder);
			}
		} else {
			storageDirectory = new File(storagePath, subFolder);
		}
	}
	
}
