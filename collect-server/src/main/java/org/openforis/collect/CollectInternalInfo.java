package org.openforis.collect;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.utils.Files;

public class CollectInternalInfo extends CollectInfo {

	private static final Logger LOG = LogManager.getLogger(CollectInternalInfo.class); 
	private static final String COLLECT_ROOT_SYS_VAR_NAME = "collect.root";
	
	private String webappsPath;
	private String rootPath;
	
	public CollectInternalInfo() {
		this.rootPath = determineRootPath();
		this.webappsPath = determineWebappsPath();
	}

	private String determineWebappsPath() {
		if (rootPath == null) {
			throw new IllegalStateException("Root path not initialized");
		}
		return new File(rootPath).getParent();
	}

	private String determineRootPath() {
		String collectRoot = System.getProperty(COLLECT_ROOT_SYS_VAR_NAME);
		LOG.info(String.format("Collect root system variable %s = %s",COLLECT_ROOT_SYS_VAR_NAME, collectRoot));
		if (collectRoot != null) {
			return collectRoot;
		} else {
			File tempFolder = Files.TEMP_FOLDER;
			String tempFolderPath = tempFolder.getAbsolutePath();
			LOG.info(String.format("Using temp folder as root: %s",tempFolderPath));
			return tempFolderPath;
		}
	}
	
	public String getRootPath() {
		return rootPath;
	}
	
	public String getWebappsPath() {
		return webappsPath;
	}
}
