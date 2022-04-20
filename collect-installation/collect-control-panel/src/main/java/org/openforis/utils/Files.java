package org.openforis.utils;

import java.io.File;
import java.io.RandomAccessFile;

import org.apache.commons.io.IOUtils;

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

	public static String tail(File file, long maxSize) {
		StringBuilder sb = new StringBuilder(Long.valueOf(maxSize).intValue());
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			long position = 0;
			long size = raf.length();
			if (size > maxSize) {
				position = size - maxSize;
				raf.seek(position);
			}
			String line = raf.readLine();
			while (line != null && position < size) {
				position += line.length() + 1;
				sb.append(line);
				sb.append('\n');
				line = raf.readLine();
			}
			return sb.toString();
		} catch (Exception e) {
			return "";
		} finally {
			IOUtils.closeQuietly(raf);
		}
	}

}
