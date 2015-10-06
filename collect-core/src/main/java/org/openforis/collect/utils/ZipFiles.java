package org.openforis.collect.utils;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class ZipFiles {

	public static void writeFile(ZipOutputStream zipOutputStream, File file, String zipEntryName) {
		try {
			zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
			byte[] metadataContent = FileUtils.readFileToByteArray(file);
			IOUtils.write(metadataContent, zipOutputStream);
			zipOutputStream.closeEntry();
		} catch (IOException e) {
			throw new RuntimeException("Error writing data to zip file", e);
		}
	}
	
}
