package org.openforis.collect;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.poi.util.IOUtils;
import org.openforis.commons.versioning.Version;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CollectInfo {
	
	private static final String INFO_PROPERTIES_FILE_NAME = "info.properties";
	private static final String VERSION_PROPERTY_KEY = CollectInfo.class.getPackage().getName() + ".version";
	private static final String VOID_VERSION = "PROJECT_VERSION"; //token was not being replaced into version.properties in previous releases
	private static final Version DEV_VERSION = new Version("3.6.2-SNAPSHOT");
	
	private static final CollectInfo INSTANCE;

	static {
		InputStream is = null;
		try {
			is = CollectInfo.class.getResourceAsStream(INFO_PROPERTIES_FILE_NAME);
			Properties properties = new Properties();
			properties.load(is);
			
			String versionValue = properties.getProperty(VERSION_PROPERTY_KEY);
			Version version;
			if ( VOID_VERSION.equals(versionValue) ) {
				version = DEV_VERSION;
			} else {
				version = new Version(versionValue);
			}
			INSTANCE = new CollectInfo(version);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public static CollectInfo getInstance() {
		return INSTANCE;
	}
	
	private Version version;
	
	private CollectInfo(Version version) {
		this.version = version;
	}

	public Version getVersion() {
		return version;
	}
	
	public String getVersionFull() {
		return version.toString();
	}
	
}
