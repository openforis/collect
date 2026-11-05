package org.openforis.collect.controlpanel.server;

import java.io.File;

import org.openforis.web.server.JettyApplicationServer;
import org.openforis.web.server.JndiDataSourceConfiguration;

public class CollectJettyServer extends JettyApplicationServer {

	public static final String WEBAPP_NAME = "collect";

	public CollectJettyServer(int port, File webappsFolder, JndiDataSourceConfiguration... jndiDsConfigurations) {
		super(port, webappsFolder, jndiDsConfigurations);
		// the JAXB 2.2 bundled with Saiku defines classes through reflection, which Java 17 forbids: disable that optimization
		System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");
	}
	
	@Override
	protected String getMainWebAppName() {
		return WEBAPP_NAME;
	}

}
