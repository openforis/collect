package org.openforis.collect;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.openforis.commons.versioning.Version;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class Collect {
	
	private static final String INFO_PROPERTIES_FILE_NAME = "info.properties";
	private static final String VERSION_PROPERTY_KEY = Collect.class.getPackage().getName() + ".version";
	private static final String VOID_VERSION = "PROJECT_VERSION"; //token was not being replaced into version.properties in previous releases
	private static final Version DEV_VERSION = new Version("3.24.0-SNAPSHOT");
	
	public static final Version VERSION;
	
	static {
		Properties properties = loadInfoProperties();

		String versionValue = properties.getProperty(VERSION_PROPERTY_KEY);

		VERSION = VOID_VERSION.equals(versionValue) ? DEV_VERSION : new Version(versionValue);
	}

	private static Properties loadInfoProperties() {
		InputStream is = null;
		try {
			is = Collect.class.getResourceAsStream(INFO_PROPERTIES_FILE_NAME);
			Properties properties = new Properties();
			properties.load(is);
			return properties;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
}
