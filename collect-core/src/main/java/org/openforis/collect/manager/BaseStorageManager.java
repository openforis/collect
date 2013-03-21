package org.openforis.collect.manager;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.model.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BaseStorageManager implements Serializable {

	private static final String DEFAULT_BASE_TEMP_SUBDIR = "temp";

	private static final long serialVersionUID = 1L;

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	private static final String CATALINA_BASE = "catalina.base";

	protected static Log LOG = LogFactory.getLog(BaseStorageManager.class);

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
			String path = base + subDir != null ? (File.separator + subDir): "";
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
	
	protected void initStoragePath(String pathConfigurationKey, String subFolder) {
		Configuration configuration = configurationManager.getConfiguration();
		String storagePath = configuration.get(pathConfigurationKey);
		if ( StringUtils.isBlank(storagePath) ) {
			if ( LOG.isInfoEnabled() ) {
				LOG.info("Path with key '" + pathConfigurationKey+ "' not configured in config table");
			}
			File rootDir = getCatalinaBaseTempFolder();
			if ( rootDir != null ) {
				LOG.info("Using system property " + CATALINA_BASE + " folder as root storage directory: " + rootDir.getAbsolutePath());
			} else {
				rootDir = getTempFolder();
				if ( rootDir != null ) {
					LOG.info("Using system property " + JAVA_IO_TMPDIR + " folder as root storage directory: " + rootDir.getAbsolutePath());
				}
			}
			if ( rootDir != null ) {
				storageDirectory = new File(rootDir, subFolder);
			}
		}
	}
	
}
