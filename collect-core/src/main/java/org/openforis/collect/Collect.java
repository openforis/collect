package org.openforis.collect;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class Collect {
	private static Properties VERSION_PROPERTIES;
	
	static {
		InputStream is = null;
		try {
			is = Collect.class.getResourceAsStream("version.properties");
			VERSION_PROPERTIES = new Properties();
			VERSION_PROPERTIES.load(is);
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
	
	public static String getVersion() {
		return VERSION_PROPERTIES.getProperty(Collect.class.getPackage().getName()+".version");
	}
	
	public static void main(String[] args) {
		System.out.println("Open Foris Collect "+Collect.getVersion());
	}
}
