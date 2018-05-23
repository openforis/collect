package org.openforis.collect.controlpanel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import org.openforis.web.server.JndiDataSourceConfiguration;

public class CollectPropertiesHandler {

	private static final String COLLECT_HTTP_PORT = "collect.http_port";
	private static final String COLLECT_WEBAPPS_LOCATION = "collect.webapps_location";
	private static final String COLLECT_DB_JNDI_NAME = "collect.db.jndiName";
	private static final String COLLECT_DB_DRIVER_CLASS_NAME = "collect.db.driverClassName";
	private static final String COLLECT_DB_URL = "collect.db.url";
	private static final String COLLECT_DB_USERNAME = "collect.db.username";
	private static final String COLLECT_DB_PASSWORD = "collect.db.password";
	private static final String COLLECT_DB_INITIAL_SIZE = "collect.db.initialSize";
	private static final String COLLECT_DB_MAX_ACTIVE = "collect.db.maxActive";
	private static final String COLLECT_DB_MAX_IDLE = "collect.db.maxIdle";

	public CollectProperties parse(Properties properties) {
		CollectProperties cp = new CollectProperties();
		
		cp.setHttpPort(getIntegerProperty(properties, COLLECT_HTTP_PORT));
		cp.setWebappsLocation(getSystemVariableReplacedProperty(properties, COLLECT_WEBAPPS_LOCATION));
		
		JndiDataSourceConfiguration dsConfig = new JndiDataSourceConfiguration();
		dsConfig.setJndiName(properties.getProperty(COLLECT_DB_JNDI_NAME));
		dsConfig.setDriverClassName(properties.getProperty(COLLECT_DB_DRIVER_CLASS_NAME));
		dsConfig.setUrl(getSystemVariableReplacedProperty(properties, COLLECT_DB_URL));
		dsConfig.setUsername(properties.getProperty(COLLECT_DB_USERNAME));
		dsConfig.setPassword(properties.getProperty(COLLECT_DB_PASSWORD));
		dsConfig.setInitialSize(getIntegerProperty(properties, COLLECT_DB_INITIAL_SIZE));
		dsConfig.setMaxActive(getIntegerProperty(properties, COLLECT_DB_MAX_ACTIVE));
		dsConfig.setMaxIdle(getIntegerProperty(properties, COLLECT_DB_MAX_IDLE));
		
		cp.setCollectDataSourceConfiguration(dsConfig);
		return cp;
	}
	
	public void write(CollectProperties cp, File outputFile) throws FileNotFoundException, IOException {
		Properties p = new Properties() {
			//sort keys by name
			private static final long serialVersionUID = 1L;
			@Override
			public synchronized Enumeration<Object> keys() {
				return Collections.enumeration(new TreeSet<Object>(super.keySet()));
			}
			@Override
			public synchronized Object setProperty(String key, String value) {
				return super.setProperty(key, value == null ? "" : value);
			}
		};
		p.setProperty(COLLECT_HTTP_PORT, "" + cp.getHttpPort());
		p.setProperty(COLLECT_WEBAPPS_LOCATION, cp.getWebappsLocation());
		JndiDataSourceConfiguration dsConfig = cp.getCollectDataSourceConfiguration();
		p.setProperty(COLLECT_DB_JNDI_NAME, dsConfig.getJndiName());
		p.setProperty(COLLECT_DB_DRIVER_CLASS_NAME, dsConfig.getDriverClassName());
		p.setProperty(COLLECT_DB_URL, dsConfig.getUrl());
		p.setProperty(COLLECT_DB_USERNAME, dsConfig.getUsername());
		p.setProperty(COLLECT_DB_PASSWORD, dsConfig.getPassword());
		p.setProperty(COLLECT_DB_INITIAL_SIZE, "" + dsConfig.getInitialSize());
		p.setProperty(COLLECT_DB_MAX_ACTIVE, "" + dsConfig.getMaxActive());
		p.setProperty(COLLECT_DB_MAX_IDLE, "" + dsConfig.getMaxIdle());
		p.store(new FileOutputStream(outputFile), "Open Foris Collect configuration file");
	}

	private String getSystemVariableReplacedProperty(Properties properties, String propName) {
		String originalValue = properties.getProperty(propName);
		if (originalValue == null || originalValue.length() == 0) {
			return originalValue;
		}
		String finalValue = originalValue;
		String[] systemProps = {"user.home"};
		for (String sysPropName : systemProps) {
			String propVal = System.getProperty(sysPropName);
			finalValue = finalValue.replace("${" + sysPropName + "}", propVal);
		}
		return finalValue;
	}

	private int getIntegerProperty(Properties properties, String key) {
		String value = properties.getProperty(key);
		return value == null ? null : Integer.parseInt(value);
	}
}
