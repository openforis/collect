package org.openforis.collect.controlpanel;

import java.io.File;
import java.io.IOException;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;

import javafx.application.Application;

@SuppressWarnings("restriction")
public class CollectControlPanelController {

	private CollectServer server;

	public void initialize() {
		int port = 8080;
		String context = "collect";
		File warFile = new File("/home/ricci/dev/projects/openforis/collect/collect-web/collect-webapp/target/collect.war");
		
		server = new CollectServer(port, context, warFile);
	}
	
	public void startServer() throws Exception {
		server.start();
	}
	
	public void stopServer() throws Exception {
		server.stop();
	}
	
	void shutdown() throws Exception {
		stopServer();
//		executorService.shutdownNow();
	}
	
	void openBrowser( Application application , final long delay ) {
		
//		final HostServicesDelegate hostServices = HostServicesFactory.getInstance( application );
	}
	
}
