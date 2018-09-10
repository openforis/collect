package org.openforis.collect.utils;

public abstract class SystemUtils {

	public static boolean isLinuxOs() {
		return "Linux".equals(System.getProperty("os.name"));
	}

	public static void runCommandQuietly(String command) {
		try {
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			p.destroy();
		 } catch (Exception e) {}
	}
}
