package org.openforis.collect;

public class Environment {

	private static final String MOBILE_VM_VENDOR = "The Android Project";

	public static boolean isAndroid() {
		return MOBILE_VM_VENDOR.equalsIgnoreCase(System.getProperty("java.vendor"));
	}
}
