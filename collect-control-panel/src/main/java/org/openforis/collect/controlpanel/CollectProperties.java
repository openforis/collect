package org.openforis.collect.controlpanel;

import java.io.File;

import org.openforis.utils.Files;
import org.openforis.web.server.JndiDataSourceConfiguration;

public class CollectProperties {

	private static final int DEFAULT_PORT = 8380;
	private static final String DEFAULT_WEBAPPS_LOCATION = Files.getCurrentLocation() + File.separator + "webapps";

	private int httpPort = DEFAULT_PORT;
	private String webappsLocation = DEFAULT_WEBAPPS_LOCATION;
	private JndiDataSourceConfiguration collectDataSourceConfiguration;
	
	public int getHttpPort() {
		return httpPort;
	}
	
	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}
	
	public String getWebappsLocation() {
		return webappsLocation;
	}
	
	public void setWebappsLocation(String webappsLocation) {
		this.webappsLocation = webappsLocation;
	}
	
	public JndiDataSourceConfiguration getCollectDataSourceConfiguration() {
		return collectDataSourceConfiguration;
	}
	
	public void setCollectDataSourceConfiguration(JndiDataSourceConfiguration collectDataSourceConfiguration) {
		this.collectDataSourceConfiguration = collectDataSourceConfiguration;
	}
	
}
