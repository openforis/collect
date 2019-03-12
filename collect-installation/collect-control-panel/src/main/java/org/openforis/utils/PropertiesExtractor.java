package org.openforis.utils;

import java.util.Properties;

public class PropertiesExtractor {
	private Properties properties;
	private String propertiesFileName;

	public PropertiesExtractor(Properties properties, String propertiesFileName) {
		super();
		this.properties = properties;
		this.propertiesFileName = propertiesFileName;
	}

	public String getSystemVariableReplacedProperty(String propName) {
		return getSystemVariableReplacedProperty(propName, null);
	}

	public String getSystemVariableReplacedProperty(String propName, String defaultValue) {
		String originalValue = getProperty(propName, defaultValue);
		String finalValue = originalValue;
		String[] systemProps = { "user.home" };
		for (String sysPropName : systemProps) {
			String propVal = System.getProperty(sysPropName);
			finalValue = finalValue.replace("${" + sysPropName + "}", propVal);
		}
		return finalValue;
	}

	public int getIntegerProperty(String key, int defaultValue) {
		String value = getProperty(key, "");
		if (Strings.isBlank(value)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new RuntimeException(String.format("Property '%s' of '%s' file must be an integer", key,
					propertiesFileName));
		}
	}

	public String getProperty(String key) {
		return getProperty(key, null);
	}

	public String getProperty(String key, String defaultValue) {
		String value = properties.getProperty(key);
		if (Strings.isBlank(value)) {
			if (defaultValue == null)
				throw new RuntimeException(
						String.format("Missing property '%s' in '%s' file", key, propertiesFileName));
			else 
				return defaultValue;
		}
		return value;
	}
}