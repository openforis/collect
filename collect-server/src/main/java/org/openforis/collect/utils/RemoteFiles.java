package org.openforis.collect.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class RemoteFiles {
	
	public static File download(String url) throws IOException {
		File tempFile = File.createTempFile("collect-temp-file", ".zip");
		return download(url, tempFile);
	}

	private static File download(String fileAddress, File destFile) throws IOException {
		FileOutputStream fos = null;
		try {
			URL url = new URL(fileAddress);
			HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
	        urlconn.setConnectTimeout(100000);
	        urlconn.setReadTimeout(10000);
	        urlconn.setRequestMethod("GET");
	        urlconn.connect();
	        fos = new FileOutputStream(destFile);
	        IOUtils.copy(urlconn.getInputStream(), fos);
			return destFile;
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}
}