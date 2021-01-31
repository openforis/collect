package org.openforis.collect.controlpanel.server;

import java.io.File;

import org.openforis.web.server.JettyApplicationServer;
import org.openforis.web.server.JndiDataSourceConfiguration;

public class CollectJettyServer extends JettyApplicationServer {

	public static final String WEBAPP_NAME = "collect";

	public CollectJettyServer(int port, File webappsFolder, JndiDataSourceConfiguration... jndiDsConfigurations) {
		super(port, webappsFolder, jndiDsConfigurations);
	}
	
	@Override
	protected String getMainWebAppName() {
		return WEBAPP_NAME;
	}

}
