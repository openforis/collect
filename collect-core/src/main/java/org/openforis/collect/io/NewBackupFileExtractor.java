package org.openforis.collect.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.utils.Files;

/**
 * 
 * @author S. Ricci
 *
 */
public class NewBackupFileExtractor implements Closeable {

	public static final String RECORD_FILE_DIRECTORY_NAME = "upload";

	private File file;
	private File tempUncompressedFolder;
	private ZipFile zipFile;

	public NewBackupFileExtractor(File file) throws ZipException, IOException {
		this.file = file;
	}

	public void init() throws IOException {
		tempUncompressedFolder = Files.createTempDirectory();
		zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = entries.nextElement();
			if (! zipEntry.isDirectory()) {
				InputStream is = zipFile.getInputStream(zipEntry);
	
				String entryName = zipEntry.getName();
				File folder = getOrCreateEntryFolder(entryName);
				String fileName = extractFileName(entryName);
				File newFile = new File(folder, fileName);
				newFile.createNewFile();
				FileUtils.copyInputStreamToFile(is, newFile);
			}
		}
	}
	
	public File extractInfoFile() {
		return extract(SurveyBackupJob.INFO_FILE_NAME);
	}
	
	public SurveyBackupInfo extractInfo() {
		try {
			File infoFile = extractInfoFile();
			SurveyBackupInfo info = SurveyBackupInfo.parse(new FileInputStream(infoFile));
			return info;
		} catch (Exception e) {
			throw new RuntimeException("Error extracting info file from archive", e);
		}
	}
	
	public File extractIdmlFile() {
		return extract(SurveyBackupJob.SURVEY_XML_ENTRY_NAME);
	}
	
	public File extract(String entryName) {
		return extract(entryName, true);
	}
	
	public File extract(String entryName, boolean required) {
		File folder = getOrCreateEntryFolder(entryName);
		String fileName = extractFileName(entryName);
		File result = new File(folder, fileName);
		return result.exists() ? result: null;
	}

	public List<String> listEntriesInPath(String path) {
		List<String> result = new ArrayList<String>();
		File folder = getOrCreateEntryFolder(path);
		File[] files = folder.listFiles();
		for (File file : files) {
			result.add(file.getName());
		}
		return result;
	}
	
	public List<String> listSpeciesEntryNames() {
		List<String> entries = listEntriesInPath(SurveyBackupJob.SPECIES_FOLDER);
		return entries;
	}
	
	public List<File> extractFilesInPath(String folder) throws IOException {
		List<File> result = new ArrayList<File>();
		List<String> entryNames = listEntriesInPath(folder);
		for (String name : entryNames) {
			File tempFile = extract(name);
			result.add(tempFile);
		}
		return result;
	}
	
	public InputStream findEntryInputStream(String entryName) throws IOException {
		File file = extract(entryName, false);
		if (file == null) {
			return null;
		} else {
			return new FileInputStream(file);
		}
	}

	public boolean containsEntry(String name) {
		File file = extract(name, false);
		return file != null;
	}
	
	public boolean containsEntriesInPath(String path) {
		List<String> entryNames = listEntriesInPath(path);
		return ! entryNames.isEmpty();
	}
	
	public List<String> getEntryNames() {
		List<String> result = new ArrayList<String>();
		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		while ( zipEntries.hasMoreElements() ) {
			ZipEntry zipEntry = zipEntries.nextElement();
			String name = zipEntry.getName();
			result.add(name);
		}
		return result;
	}
	
	public boolean isIncludingRecordFiles() {
		File recordFilesDir = new File(tempUncompressedFolder, RECORD_FILE_DIRECTORY_NAME);
		return recordFilesDir.exists() && recordFilesDir.isDirectory();
	}

	public boolean isOldFormat() {
		return ! containsEntry(SurveyBackupJob.INFO_FILE_NAME);
	}
	
	public int size() {
		return zipFile.size();
	}
	
	private File getOrCreateEntryFolder(String entryName) {
		String path = FilenameUtils.getPathNoEndSeparator(entryName);
		String[] entryParts = path.split("\\|/");
		File folder = tempUncompressedFolder;
		for (int i = 0; i < entryParts.length; i++) {
			String part = entryParts[i];
			folder = new File(folder, part);
		}
		if (! folder.exists()) {
			folder.mkdirs();
		}
		return folder;
	}

	private String extractFileName(String entryName) {
		String name = FilenameUtils.getName(entryName);
		return name;
	}

	@Override
	public void close() throws IOException {
		if (zipFile != null ) {
			zipFile.close();
		}
		FileUtils.deleteQuietly(tempUncompressedFolder);
	}
	
}