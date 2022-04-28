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

import org.apache.commons.io.IOUtils;
import org.openforis.collect.utils.Files;
import org.openforis.collect.utils.ZipFiles;
import org.openforis.commons.versioning.Version;
import org.openforis.concurrency.ProgressListener;

/**
 * 
 * @author S. Ricci
 *
 */
public class NewBackupFileExtractor implements Closeable {

	public static final String RECORD_FILE_DIRECTORY_NAME = "upload";
	
	//input variables
	private File file;
	
	//temporary variables
	private transient File tempUncompressedFolder;
	private transient ZipFile zipFile;
	private Boolean oldFormat = null;
	private SurveyBackupInfo info;

	public NewBackupFileExtractor(File file) throws ZipException, IOException {
		this.file = file;
	}

	public void init() throws IOException {
		init(null);
	}
	
	public void init(ProgressListener progressListener) throws IOException {
		tempUncompressedFolder = Files.createTempDirectory();
		zipFile = new ZipFile(file);
		ZipFiles.extract(zipFile, tempUncompressedFolder, progressListener);
	}
	
	public File extractInfoFile() throws IOException {
		return extract(SurveyBackupJob.INFO_FILE_NAME);
	}
	
	public SurveyBackupInfo getInfo() throws IOException {
		if (info == null) {
			if (isOldFormat()) {
				info = new SurveyBackupInfo();
				info.setCollectVersion(new Version("3.9.0"));
			} else {
				info = extractInfo();
			}
		}
		return info;
	}
	
	public SurveyBackupInfo extractInfo() throws IOException {
		try {
			File infoFile = extractInfoFile();
			SurveyBackupInfo info = SurveyBackupInfo.parse(new FileInputStream(infoFile));
			return info;
		} catch (Exception e) {
			throw new IOException("Error extracting info file from archive", e);
		}
	}
	
	public File extractIdmlFile() throws IOException {
		return extract(SurveyBackupJob.SURVEY_XML_ENTRY_NAME);
	}
	
	public File extract(String entryName) throws IOException {
		return extract(entryName, true);
	}
	
	public File extract(String entryName, boolean required) throws IOException {
		File folder = ZipFiles.getOrCreateEntryFolder(tempUncompressedFolder, entryName);
		String fileName = Files.extractFileName(entryName);
		File result = new File(folder, fileName);
		if (!result.getCanonicalPath().startsWith(tempUncompressedFolder.getCanonicalPath())) {
			throw new IOException("Entry is outside of target directory");
		}
		return result.exists() ? result: null;
	}

	public List<String> listSpeciesEntryNames() throws IOException {
		List<String> entries = Files.listFileNamesInFolder(tempUncompressedFolder, SurveyBackupJob.SPECIES_FOLDER);
		return entries;
	}
	
	public List<File> extractFilesInPath(String folder) throws IOException {
		List<File> result = new ArrayList<File>();
		List<String> entryNames = Files.listFileNamesInFolder(tempUncompressedFolder, folder);
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
		try {
			File file = extract(name, false);
			return file != null;
		} catch (IOException e) {
			return false;
		}
	}
	
	public boolean containsEntriesInPath(String path) throws IOException {
		List<String> fileNames = listFilesInFolder(path);
		return ! fileNames.isEmpty();
	}
	
	public List<String> listFilesInFolder(String path) throws IOException {
		return Files.listFileNamesInFolder(tempUncompressedFolder, path);
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
		if (oldFormat == null) {
			oldFormat = !containsEntry(SurveyBackupJob.INFO_FILE_NAME);
		}
		return oldFormat;
	}
	
	public int size() {
		return zipFile.size();
	}
	
	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(zipFile);
		Files.deleteFolder(tempUncompressedFolder);
	}

}