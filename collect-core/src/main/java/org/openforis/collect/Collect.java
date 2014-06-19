package org.openforis.collect;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.openforis.commons.versioning.Version;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class Collect {
	
	private static final String VOID_VERSION = "PROJECT_VERSION"; //token was not being replaced into version.properties in previous releases
	
	private static Properties VERSION_PROPERTIES;
	private static final Version VERSION;
	
	static {
		InputStream is = null;
		try {
			is = Collect.class.getResourceAsStream("version.properties");
			VERSION_PROPERTIES = new Properties();
			VERSION_PROPERTIES.load(is);
			
			String versionValue = VERSION_PROPERTIES.getProperty(Collect.class.getPackage().getName()+".version");
			if ( VOID_VERSION.equals(versionValue) ) {
				VERSION = null;
			} else {
				VERSION = new Version(versionValue);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if ( is != null ) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static Version getVersion() {
		return VERSION;
	}
	
//	public static void main(String[] args) {
//		System.out.println("Open Foris Collect "+Collect.getVersion());
//	}
}
