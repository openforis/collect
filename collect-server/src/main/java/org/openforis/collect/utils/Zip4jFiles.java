package org.openforis.collect.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;

/**
 * 
 * @author S. Ricci
 *
 */
public class Zip4jFiles {
	
	@SuppressWarnings("unchecked")
	public static void copyFiles(ZipFile sourceFile, ZipFile destFile, ZipParameters zipParameters) throws ZipException, IOException {
		for (FileHeader header : (List<FileHeader>) sourceFile.getFileHeaders()) {
			if (! header.isDirectory()) {
				ZipInputStream is = sourceFile.getInputStream(header);
				File tempFile = File.createTempFile("temp_folder", "");
				IOUtils.copy(is, new FileOutputStream(tempFile));
				addFile(destFile, tempFile, header.getFileName(), zipParameters);
			}
		}
	}
	
	public static void addFile(ZipFile destFile, File file, String fileNameInZip, ZipParameters zipParameters) throws ZipException {
		zipParameters.setSourceExternalStream(true);
		zipParameters.setFileNameInZip(fileNameInZip);
		destFile.addFile(file, zipParameters);
	}
	
	public static void addFile(ZipFile destFile, InputStream inputStream, String fileNameInZip, ZipParameters zipParameters) throws ZipException {
		zipParameters.setSourceExternalStream(true);
		zipParameters.setFileNameInZip(fileNameInZip);
		destFile.addStream(inputStream, zipParameters);
	}
}