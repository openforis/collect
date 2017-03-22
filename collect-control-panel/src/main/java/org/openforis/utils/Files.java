package org.openforis.utils;

import java.io.File;

public abstract class Files {
	
	public static String getCurrentLocation() {
		return new File(".").getAbsoluteFile().getParentFile().getAbsolutePath();
	}
	
	public static String getUserHomeLocation() {
		return System.getProperty("user.home");
	}
	
	public static String getLocation(String... parts) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			sb.append(parts[i]);
			if (i < parts.length - 1) {
				sb.append(File.separatorChar);
			}
		}
		return sb.toString();
	}

}
