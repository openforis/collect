package org.openforis.collect.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class OpenForisIOUtils {

	public static final String UTF_8 = "UTF-8";

	public static File copyToTempFile(InputStream is) {
		return copyToTempFile(toReader(is));
	}
		
	public static File copyToTempFile(Reader reader) {
		File tempFile = createTempFile();
		FileOutputStream tempOut = null;
		try {
			tempOut = new FileOutputStream(tempFile);
			IOUtils.copy(reader, tempOut, UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("Error copying to temp file: " + e.getMessage());
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(tempOut);
		}
		return tempFile;
	}

	public static File createTempFile() {
		return createTempFile(1);
	}
	
	private static File createTempFile(int tempative) {
		try {
			String fileName = UUID.randomUUID().toString();
			String tmpDirPath = System.getProperty("java.io.tmpdir");
			File file = new File(tmpDirPath, fileName);
			if ( file.exists() ) {
				if ( tempative < 5 ) {
					return createTempFile(tempative + 1);
				} else {
					throw new RuntimeException("Cannot creating temp file (name already exists): " + fileName);
				}
			} else if ( file.createNewFile() ) {
				return file;
			} else {
				throw new RuntimeException("Cannot creating temp file");
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot creating temp file: " + e.getMessage());
		}
	}

	public static InputStreamReader toReader(InputStream is) {
		try {
			return new InputStreamReader(is, UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
