package org.openforis.collect.controlpanel;

import java.io.File;

import org.openforis.utils.Files;
import org.openforis.web.server.JndiDataSourceConfiguration;

public class CollectProperties {

	private static final int DEFAULT_PORT = 8380;
	private static final String DEFAULT_JNDI_NAME = "jdbc/collectDs";
	private static final String DEFAULT_DB_DRIVER = "org.sqlite.JDBC";
	private static final String DEFAULT_DB_URL = "jdbc:sqlite:${user.home}/OpenForis/Collect/data/collect.db";
	private static final int DEFAULT_DB_INITIAL_SIZE = 5;
	private static final int DEFAULT_DB_MAX_ACTIVE = 20;
	private static final int DEFAULT_DB_MAX_IDLE = 5;
	private static final String DEFAULT_DB_PASSWORD = "";
	private static final String DEFAULT_DB_USERNAME = "";

	private int httpPort = DEFAULT_PORT;
	private String webappsLocation;
	private JndiDataSourceConfiguration collectDataSourceConfiguration = defaultDSConfiguration();
	
	private static JndiDataSourceConfiguration defaultDSConfiguration() {
		JndiDataSourceConfiguration c = new JndiDataSourceConfiguration();
		c.setJndiName(DEFAULT_JNDI_NAME);
		c.setDriverClassName(DEFAULT_DB_DRIVER);
		c.setUrl(DEFAULT_DB_URL);
		c.setUsername(DEFAULT_DB_USERNAME);
		c.setPassword(DEFAULT_DB_PASSWORD);
		c.setInitialSize(DEFAULT_DB_INITIAL_SIZE);
		c.setMaxActive(DEFAULT_DB_MAX_ACTIVE);
		c.setMaxIdle(DEFAULT_DB_MAX_IDLE);
		return c;
	}

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
