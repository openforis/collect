package org.openforis.web.server;

import java.util.Properties;

public class JndiDataSourceConfiguration {

	private String jndiName;
	private String driverClassName;
	private String url;
	private String username;
	private String password;
	private Integer initialSize;
	private Integer maxActive;
	private Integer maxIdle;

	public Properties toProperties() {
		Properties properties = new Properties();
		properties.setProperty("driverClassName", driverClassName);
		properties.setProperty("url", url);
		properties.setProperty("username", username);
		properties.setProperty("password", password);
		setIntegerProperty(properties, "initialSize", initialSize);
		setIntegerProperty(properties, "maxActive", maxActive);
		setIntegerProperty(properties, "maxIdle", maxIdle);
		return properties;
	}
	
	private void setIntegerProperty(Properties properties, String key, Integer value) {
		String strVal = value == null ? null: value.toString();
		properties.setProperty(key, strVal);
	}
	
	public String getJndiName() {
		return jndiName;
	}
	
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}
	
	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(Integer initialSize) {
		this.initialSize = initialSize;
	}

	public Integer getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(Integer maxActive) {
		this.maxActive = maxActive;
	}

	public Integer getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}
}
