package org.openforis.collect;

import java.io.File;

import org.openforis.collect.utils.Files;

public class CollectInternalInfo extends CollectInfo {

	private static final String COLLECT_ROOT_SYS_VAR_NAME = "collect.root";
	private String webappsPath;
	private String rootPath;
	
	public CollectInternalInfo() {
		super();
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
		if (collectRoot != null) {
			return collectRoot;
		} else {
			File tempFolder = Files.TEMP_FOLDER;
			return tempFolder.getAbsolutePath();
		}
	}
	
	public String getRootPath() {
		return rootPath;
	}
	
	public String getWebappsPath() {
		return webappsPath;
	}
}
