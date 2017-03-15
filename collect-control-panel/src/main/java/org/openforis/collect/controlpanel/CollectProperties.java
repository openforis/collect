package org.openforis.collect.controlpanel;

import org.openforis.web.server.JndiDataSourceConfiguration;

public class CollectProperties {

	private int httpPort;
	private String webappsLocation;
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
