package org.openforis.collect.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class Files {

	public static File writeToTempFile(String text, String tempFilePrefix, String tempFileSuffix) throws IOException {
		File file = File.createTempFile(tempFilePrefix, tempFileSuffix);
		Writer writer = null;
		try {
			writer = new FileWriter(file);
			IOUtils.write(text.getBytes(), writer, "UTF-8");			
		} finally {
			IOUtils.closeQuietly(writer);
		}
		return file;
	}
	
	public static File createTempDirectory() throws IOException {
		File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
		if (! temp.delete()) {
			throw new IOException("Could not delete temp file: "
					+ temp.getAbsolutePath());
		}
		if (! temp.mkdir()) {
			throw new IOException("Could not create temp directory: "
					+ temp.getAbsolutePath());
		}

		return (temp);
	}

	public static void eraseFileContent(File file) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write("");
			writer.flush();
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
	
}
