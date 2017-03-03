package org.openforis.collect.controlpanel;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class CollectServer {

	private int port = 8080;
	private String context;
	private File warFile;
	
	private Server server;

	public CollectServer(int port, String context, File warFile) {
		super();
		this.port = port;
		this.context = context;
		this.warFile = warFile;
	}

	public void start() throws Exception {

		server = new Server(port);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(context);
		webapp.setWar(warFile.getAbsolutePath());

		server.setHandler(webapp);
		
		server.start();
		
		server.join();
	}

	public void stop() throws Exception {
		server.stop();
	}
}
