package org.openforis.collect.utils;

import java.io.IOException;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class SystemUtils {

	private static final OS CURRENT_OS = determineCurrentOs();
	
	private static OS determineCurrentOs() {
		String osName = System.getProperty("os.name").toLowerCase();
		for (OS os : OS.values()) {
			if (osName.startsWith(os.name)) {
				return os;
			}
		}
		return OS.OTHER;
	}
	
	private enum OS {
		WINDOWS("windows"), 
		LINUX("linux"), 
		MacOS("mac"),
		OTHER("");
		
		private String name;
		
		OS(String name) {
			this.name = name;
		}
	}
	
	public static boolean isLinux() {
		return CURRENT_OS == OS.LINUX;
	}
	
	public static boolean isWindows() {
		return CURRENT_OS == OS.WINDOWS;
	}
	
	public static void runCommandQuietly(String command) {
		try {
			runCommand(command);
		} catch (Exception e) {}
	}

	public static void runCommand(String command) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
		p.destroy();
	}
}
