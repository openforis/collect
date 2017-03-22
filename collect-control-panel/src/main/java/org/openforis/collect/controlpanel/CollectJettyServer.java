package org.openforis.collect.controlpanel;

import java.io.File;

import org.openforis.web.server.JndiDataSourceConfiguration;

public class CollectJettyServer extends JettyApplicationServer {

	public CollectJettyServer(int port, File webappsFolder, File logFile, JndiDataSourceConfiguration... jndiDsConfigurations) {
		super(port, webappsFolder, logFile, jndiDsConfigurations);
	}
	
	@Override
	protected String getMainWebAppName() {
		return "collect";
	}

}
