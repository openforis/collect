package org.openforis.collect.controlpanel;

import java.io.File;

import org.openforis.web.server.JndiDataSourceConfiguration;

public class CollectJettyServer extends JettyApplicationServer {

	public CollectJettyServer(int port, File webappsFolder, JndiDataSourceConfiguration... jndiDsConfigurations) {
		super(port, webappsFolder, jndiDsConfigurations);
	}
	
	@Override
	protected String getMainWebAppName() {
		return "collect";
	}

	@Override
	protected String getDefaultLogFileLocation() {
		String currentFolder = System.getProperty("user.dir");
		String webappsFolder = currentFolder + File.separator + "webapps";
		String collectWebappLocation = webappsFolder + File.separator + getMainWebAppName();
		String collectLogFileLocation = collectWebappLocation + "/logs/collect.log";
		return collectLogFileLocation;
	}
}
