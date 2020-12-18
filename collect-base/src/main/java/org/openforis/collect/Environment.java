package org.openforis.collect;

public class Environment {

	private static final String MOBILE_VM_VENDOR = "The Android Project";

	public static boolean isAndroid() {
		return MOBILE_VM_VENDOR.equalsIgnoreCase(System.getProperty("java.vendor"));
	}

	public static boolean isServerJetty() {
		return classExists("org.eclipse.jetty.server.Server");
	}

	public static boolean isServerTomcat() {
		return classExists("org.apache.catalina.startup.Tomcat");
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
