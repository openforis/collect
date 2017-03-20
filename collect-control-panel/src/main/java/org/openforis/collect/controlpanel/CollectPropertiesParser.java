package org.openforis.collect.controlpanel;

import java.util.Properties;

import org.openforis.web.server.JndiDataSourceConfiguration;

public class CollectPropertiesParser {

	public CollectProperties parse(Properties properties) {
		CollectProperties collectProp = new CollectProperties();
		
		collectProp.setHttpPort(getIntegerProperty(properties, "collect.http_port"));
		collectProp.setWebappsLocation(getSystemVariableReplacedProperty(properties, "collect.webapps_location"));
		
		JndiDataSourceConfiguration collectDsConfig = new JndiDataSourceConfiguration();
		collectDsConfig.setJndiName(properties.getProperty("collect.db.jndiName"));
		collectDsConfig.setDriverClassName(properties.getProperty("collect.db.driverClassName"));
		collectDsConfig.setUrl(getSystemVariableReplacedProperty(properties, "collect.db.url"));
		collectDsConfig.setUsername(properties.getProperty("collect.db.username"));
		collectDsConfig.setPassword(properties.getProperty("collect.db.password"));
		collectDsConfig.setInitialSize(getIntegerProperty(properties, "collect.db.initialSize"));
		collectDsConfig.setMaxActive(getIntegerProperty(properties, "collect.db.maxActive"));
		collectDsConfig.setMaxIdle(getIntegerProperty(properties, "collect.db.maxIdle"));
		
		collectProp.setCollectDataSourceConfiguration(collectDsConfig);
		return collectProp;
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
