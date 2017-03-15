package org.openforis.collect.controlpanel;

import java.util.Properties;

import org.openforis.web.server.JndiDataSourceConfiguration;

public class CollectPropertiesParser {

	public CollectProperties parse(Properties properties) {
		CollectProperties collectProp = new CollectProperties();
		
		collectProp.setHttpPort(getIntegerProperty(properties, "collect.http_port"));
		collectProp.setWebappsLocation(properties.getProperty("collect.webapps_location"));
		
		JndiDataSourceConfiguration collectDsConfig = new JndiDataSourceConfiguration();
		collectDsConfig.setJndiName(properties.getProperty("collect.db.jndiName"));
		collectDsConfig.setDriverClassName(properties.getProperty("collect.db.driverClassName"));
		collectDsConfig.setUrl(extractDbUrl(properties));
		collectDsConfig.setUsername(properties.getProperty("collect.db.username"));
		collectDsConfig.setPassword(properties.getProperty("collect.db.password"));
		collectDsConfig.setInitialSize(getIntegerProperty(properties, "collect.db.initialSize"));
		collectDsConfig.setMaxActive(getIntegerProperty(properties, "collect.db.maxActive"));
		collectDsConfig.setMaxIdle(getIntegerProperty(properties, "collect.db.maxIdle"));
		
		collectProp.setCollectDataSourceConfiguration(collectDsConfig);
		return collectProp;
	}

	private String extractDbUrl(Properties properties) {
		String url = properties.getProperty("collect.db.url");
		String variableReplacedUrl = url;
		String[] systemProps = {"user.home"};
		for (String propName : systemProps) {
			String propVal = System.getProperty(propName);
			variableReplacedUrl = variableReplacedUrl.replace("${" + propName + "}", propVal);
		}
		return variableReplacedUrl;
	}

	private int getIntegerProperty(Properties properties, String key) {
		String value = properties.getProperty(key);
		return value == null ? null : Integer.parseInt(value);
	}
	
}
