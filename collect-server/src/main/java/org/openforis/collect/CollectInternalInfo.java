package org.openforis.collect;

import java.io.File;

public class CollectInternalInfo extends CollectInfo {

	private static final String COLLECT_ROOT_SYS_VAR_NAME = "collect.root";
	private String rootPath;
	private String webappsPath;
	
	public CollectInternalInfo() {
		super();
		this.rootPath = determineRootPath();
		this.webappsPath = new File(this.rootPath).getParent();
	}

	private String determineRootPath() {
		String collectRoot = System.getProperty(COLLECT_ROOT_SYS_VAR_NAME);
		if (collectRoot != null) {
			return collectRoot;
		} else {
			String sysVarName = COLLECT_ROOT_SYS_VAR_NAME;
			throw new IllegalStateException("Collect root path not set on system variable " + sysVarName);
		}
	}
	
	public String getWebappsPath() {
		return webappsPath;
	}
}
