package org.openforis.collect.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.concurrency.Progress;
import org.openforis.concurrency.ProgressListener;

/**
 * 
 * @author S. Ricci
 *
 */
public class ZipFiles {

	public static void writeFile(ZipOutputStream zipOutputStream, File file, String zipEntryName) {
		FileInputStream fileInputStream = null;
		try {
			zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
			fileInputStream = new FileInputStream(file);
			IOUtils.copy(fileInputStream, zipOutputStream);
			zipOutputStream.closeEntry();
		} catch (IOException e) {
			throw new RuntimeException("Error writing data to zip file", e);
		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}
	}
	
	public static void extract(File file, File destinationFolder) throws IOException {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			extract(zipFile, destinationFolder);
		} finally {
			IOUtils.closeQuietly(zipFile);
		}
	}
	
	public static void extract(ZipFile zipFile, File destinationFolder) throws IOException {
		extract(zipFile, destinationFolder, null);
	}
	
	public static void extract(ZipFile zipFile, File destinationFolder, ProgressListener progressListener) throws IOException {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		if (progressListener != null) {
			progressListener.progressMade(new Progress(0, zipFile.size()));
		}
		int count = 0;
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = entries.nextElement();
			if (! zipEntry.isDirectory()) {
				String entryName = zipEntry.getName();
				File folder = getOrCreateEntryFolder(destinationFolder, entryName);
				String fileName = Files.extractFileName(entryName);
				String fileNameNormalized = FilenameUtils.normalize(fileName);
				File newFile = new File(folder, fileNameNormalized);
				checkIsExtractingFileInsideFolder(newFile, destinationFolder);
				newFile.createNewFile();
				InputStream is = zipFile.getInputStream(zipEntry);
				FileUtils.copyInputStreamToFile(is, newFile);
			}
			count ++;
			if (progressListener != null) {
				progressListener.progressMade(new Progress(count, zipFile.size()));
			}
		}
	}

	private static void checkIsExtractingFileInsideFolder(File file, File destinationFolder) throws IOException {
		String path = file.getCanonicalPath();
		String destinationPath = destinationFolder.getCanonicalPath();
		if (!path.startsWith(destinationPath)) {
			throw new IOException(String.format("Trying to extract entry %s outside of destination folder %s", path, destinationPath));
		}
	}
	
	public File extractEntry(File parentDestinationFolder, String entryName) throws IOException {
		return extractEntry(parentDestinationFolder, entryName, true);
	}
	
	public static File extractEntry(File parentDestinationFolder, String entryName, boolean required) throws IOException {
		File folder = getOrCreateEntryFolder(parentDestinationFolder, entryName);
		String fileName = Files.extractFileName(entryName);
		File result = new File(folder, fileName);
		if (result.exists()) return result;
		if (required) throw new IOException("Entry not found");
		return null;
	}

	public static File getOrCreateEntryFolder(File parentDestinationFolder, String entryName) throws IOException {
		String path = FilenameUtils.getPathNoEndSeparator(entryName);
		return Files.getOrCreateFolder(parentDestinationFolder, path);
	}

	public static List<String> getEntryNames(ZipFile file) {
		List<String> result = new ArrayList<String>();
		Enumeration<? extends ZipEntry> zipEntries = file.entries();
		while ( zipEntries.hasMoreElements() ) {
			ZipEntry zipEntry = zipEntries.nextElement();
			String name = zipEntry.getName();
			result.add(name);
		}
		return result;
	}

	
}
