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

	private static final long serialVersionUID = 1L;

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	private static final String CATALINA_BASE = "catalina.base";

	protected static Log LOG = LogFactory.getLog(BaseStorageManager.class);

	@Autowired
	protected transient ConfigurationManager configurationManager;

	protected String storagePath;
	protected File storageDirectory;

	protected String getBaseOrTempPath() {
		String base = null;
		String[] systemProps = new String[]{CATALINA_BASE, JAVA_IO_TMPDIR};
		for (int i = 0; i < systemProps.length; i++) {
			String prop = systemProps[i];
			base = System.getProperty(prop);
			if ( base != null ) {
				if ( LOG.isInfoEnabled() ) {
					LOG.info("Using " + prop + " directory: " + base);
				}
				break;
			}
		}
		return base;
	}
	
	protected void initStoragePath(String pathConfigurationKey, String subFolder) {
		storageDirectory = null;
		Configuration configuration = configurationManager.getConfiguration();
		storagePath = configuration.get(pathConfigurationKey);
		if ( StringUtils.isBlank(storagePath) ) {
			if ( LOG.isInfoEnabled() ) {
				LOG.info("Path with key '" + pathConfigurationKey+ "' not configured in config table");
			}
			String base = getBaseOrTempPath();
			if ( base != null ) {
				storagePath = base + File.separator + subFolder;
				storageDirectory = new File(storagePath);
			}
		}
	}
	
}
