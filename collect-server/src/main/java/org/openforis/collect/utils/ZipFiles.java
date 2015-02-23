package org.openforis.collect.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class ZipFiles {
	
	@SuppressWarnings("unchecked")
	public static void copyFiles(ZipFile sourceFile, ZipFile destFile) throws ZipException, IOException {
		for (FileHeader header : (List<FileHeader>) sourceFile.getFileHeaders()) {
			if (! header.isDirectory()) {
				ZipInputStream is = sourceFile.getInputStream(header);
				File tempFile = File.createTempFile("temp_folder", "");
				IOUtils.copy(is, new FileOutputStream(tempFile));
				addFile(destFile, tempFile, header.getFileName());
			}
		}
	}
	
	public static void addFile(ZipFile destFile, File file, String fileNameInZip) throws ZipException {
		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setSourceExternalStream(true);
		zipParameters.setFileNameInZip(fileNameInZip);
		destFile.addFile(file, zipParameters);
	}
}