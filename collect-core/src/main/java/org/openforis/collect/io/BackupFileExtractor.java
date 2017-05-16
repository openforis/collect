package org.openforis.collect.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * 
 * @author S. Ricci
 *
 */
public class BackupFileExtractor extends ZipFileExtractor implements Closeable {

	public static final String RECORD_FILE_DIRECTORY_NAME = "upload";

	public BackupFileExtractor(File file) throws ZipException, IOException {
		super(file);
	}
	
	public BackupFileExtractor(ZipFile zipFile) {
		super(zipFile);
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
	
	public File extractDataSummaryFile() {
		return extract(SurveyBackupJob.DATA_SUMMARY_ENTRY_NAME, false);
	}
	
	public List<String> listSpeciesEntryNames() {
		List<String> entries = listEntriesInPath(SurveyBackupJob.SPECIES_FOLDER);
		return entries;
	}
	
	public boolean isIncludingRecordFiles() {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			if ( zipEntry.getName().startsWith(RECORD_FILE_DIRECTORY_NAME)) {
				return true;
			}
		}
		return false;
	}

	public boolean isOldFormat() {
		return ! containsEntry(SurveyBackupJob.INFO_FILE_NAME);
	}
	
	public int size() {
		return zipFile.size();
	}

	@Override
	public void close() throws IOException {
		if (zipFile != null ) {
			zipFile.close();
		}
	}
	
}