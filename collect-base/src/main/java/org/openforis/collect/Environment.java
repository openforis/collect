package org.openforis.collect;

public class Environment {

	// System props
	private static final String JAVA_VENDOR_SYSTEM_PROP = "java.vendor";
	private static final String JAVA_VERSION_SYSTEM_PROP = "java.version";
	private static final String JRE_VERSION_SYSTEM_PROP = "java.runtime.version";
	
	// Vendor names
	private static final String MOBILE_VM_VENDOR = "The Android Project";

	public static boolean isAndroid() {
		return MOBILE_VM_VENDOR.equalsIgnoreCase(System.getProperty(JAVA_VENDOR_SYSTEM_PROP));
	}

	public static boolean isServerJetty() {
		return classExists("org.eclipse.jetty.server.Server");
	}

	public static boolean isServerTomcat() {
		return classExists("org.apache.catalina.startup.Tomcat");
	}

	public static String getJREVersion() {
		String vendor = System.getProperty(JAVA_VENDOR_SYSTEM_PROP);
		String version = System.getProperty(JRE_VERSION_SYSTEM_PROP);
		if (version == null) {
			version = System.getProperty(JAVA_VERSION_SYSTEM_PROP);
		}
		return String.format("%s - %s", vendor, version);
	}

	private static boolean classExists(final String className) {
		try {
			Class.forName(className);
			return true;
		} catch (final ClassNotFoundException cnfe) {
			return false;
		}
	}
}
