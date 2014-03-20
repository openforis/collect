package org.openforis.collect.io.data;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import liquibase.util.StringUtils;

import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.exception.DataParsingExeption;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.utils.OpenForisIOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class BackupDataExtractor implements Closeable {

	private static final String ZIP_ENTRY_PATH_SEPARATOR = "/";

	protected static final String IDML_FILE_NAME = "idml.xml";

	//params
	private CollectSurvey survey;
	protected File file;
	
	//transient
	private boolean initialized;
	protected ZipFile zipFile;
	protected Enumeration<? extends ZipEntry> zipEntries;
	private DataUnmarshaller dataUnmarshaller;
	private BackupFileExtractor fileExtractor;
	private String rootEntity;
	private Step step;
	private String dataFolderPath = null;

	public BackupDataExtractor(CollectSurvey survey, File file, String rootEntity, Step step) throws ZipException, IOException {
		this(survey, new ZipFile(file), rootEntity, step);
	}
	
	public BackupDataExtractor(CollectSurvey survey, ZipFile zipFile, String rootEntity, Step step) {
		this(survey, zipFile, rootEntity, step, SurveyBackupJob.DATA_FOLDER);
	}
	
	public BackupDataExtractor(CollectSurvey survey, ZipFile zipFile, String rootEntity, Step step, String dataFolderPath) {
		this.survey = survey;
		this.zipFile = zipFile;
		this.initialized = false;
		this.fileExtractor = new BackupFileExtractor(zipFile);
		this.rootEntity = rootEntity;
		this.step = step;
		this.dataFolderPath = dataFolderPath;
	}

	public void init() throws ZipException, IOException {
		this.dataUnmarshaller = new DataUnmarshaller(new DataHandler(survey));
		this.zipEntries = zipFile.entries();
		this.initialized = true;
	}

	public ParseRecordResult nextRecord() throws Exception {
		checkInitialized();
		ParseRecordResult result = null;
		ZipEntry zipEntry = nextDataEntry();
		while ( zipEntry != null ) {
			String entryName = zipEntry.getName();
			if ( BackupRecordEntry.isValidRecordEntry(zipEntry, dataFolderPath) ) {
				BackupRecordEntry recordEntry = BackupRecordEntry.parse(entryName, dataFolderPath);
				if ( isToBeExported(recordEntry) ) {
					InputStream is = zipFile.getInputStream(zipEntry);
					return parse(is);
				} else {
					zipEntry = nextDataEntry();
				}
			}
		}
		return result;
	}

	private boolean isToBeExported(BackupRecordEntry recordEntry) {
		return (step == null || step == recordEntry.getStep()) && 
				(rootEntity == null || rootEntity.equals(recordEntry.getRootEntity()));
	}
	
	private ParseRecordResult parse(InputStream inputStream) throws IOException {
		InputStreamReader reader = OpenForisIOUtils.toReader(inputStream);
		ParseRecordResult result = dataUnmarshaller.parse(reader);
		if ( result.isSuccess() ) {
			CollectRecord record = result.getRecord();
			record.updateRootEntityKeyValues();
			record.updateEntityCounts();
		}
		return result;
	}

	public ZipEntry nextDataEntry() {
		while ( zipEntries.hasMoreElements() ) {
			ZipEntry zipEntry = zipEntries.nextElement();
			if ( BackupRecordEntry.isValidRecordEntry(zipEntry, dataFolderPath) ) {
				return zipEntry;
			}
		}
		return null;
	}
	
	public ParseRecordResult findRecord(BackupRecordEntry recordEntry) throws IOException {
		String entryName = recordEntry.getName();
		InputStream is = fileExtractor.findEntryInputStream(entryName);
		if ( is == null ) {
			return null;
		} else {
			ParseRecordResult result = parse(is);
			return result;
		}
	}
	
	@Override
	public void close() throws IOException {
		if ( zipFile != null ) {
			zipFile.close();
		}
	}
	
	protected void checkInitialized() {
		if ( ! initialized ) {
			throw new IllegalStateException("Extractor not inited");
		}
	}
	
	public long countRecords() throws DataParsingExeption {
		checkInitialized();
		int count = 0;
		ZipEntry entry = nextDataEntry();
		while ( entry != null ) {
			String entryName = entry.getName();
			BackupRecordEntry recordEntry = BackupRecordEntry.parse(entryName, dataFolderPath);
			if ( isToBeExported(recordEntry) ) {
				count++;
			}
			entry = nextDataEntry();
		}
		return count;
	}
	
	class BackupDataExtractorException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public BackupDataExtractorException() {
		}

		public BackupDataExtractorException(String message) {
			super(message);
		}
		
	}

	public static class BackupRecordEntry {
		
		private String rootEntity;
		private Step step;
		private int recordId;
		
		public BackupRecordEntry(String rootEntity, Step step, int recordId) {
			this.rootEntity = rootEntity;
			this.step = step;
			this.recordId = recordId;
		}
		
		public static boolean isValidRecordEntry(ZipEntry zipEntry) {
			return isValidRecordEntry(zipEntry, SurveyBackupJob.DATA_FOLDER);
		}
		
		public static boolean isValidRecordEntry(ZipEntry zipEntry, String basePath) {
			if ( ! basePath.endsWith(ZIP_ENTRY_PATH_SEPARATOR) ) {
				basePath += ZIP_ENTRY_PATH_SEPARATOR;
			}
			String name = zipEntry.getName();
			return ! zipEntry.isDirectory() && name.startsWith(basePath);
		}
		
		public static BackupRecordEntry parseOldFormat(String zipEntryName, String basePath, String rootEntity) throws DataParsingExeption {
			String pathSeparatorPattern = getPathSeparatorsPattern();
			
			String format = "(\\d+)" + pathSeparatorPattern + "(\\w+)\\.xml";
			
			Pattern pattern = Pattern.compile(format);
			Matcher matcher = pattern.matcher(zipEntryName);
			if ( matcher.matches() ) {
				String stepNumStr = matcher.group(1);
				String recordIdStr = matcher.group(2);
				int stepNumber = Integer.parseInt(stepNumStr);
				Step step = Step.valueOf(stepNumber);
				int recordId = Integer.parseInt(recordIdStr);
				BackupRecordEntry result = new BackupRecordEntry(rootEntity, step, recordId);
				return result;
			} else {
				throw new DataParsingExeption("Packaged file format exception: wrong zip entry name: " + zipEntryName + " expected: " + basePath + "...");
			}
		}

		private static String getPathSeparatorsPattern() {
			String[] pathSeparators = new String[] {"\\", "/"}; //for backwards compatibility, allow both \ and / as path separators
			String pathSeparatorPattern = "[";
			for (String separator : pathSeparators) {
				pathSeparatorPattern += Pattern.quote(separator);
			}
			pathSeparatorPattern += "]";
			return pathSeparatorPattern;
		}
		
		public static BackupRecordEntry parse(String zipEntryName, String basePath) throws DataParsingExeption {
			String pathSeparatorPattern = getPathSeparatorsPattern();
			
			String format = Pattern.quote(StringUtils.trimToEmpty(basePath)) + pathSeparatorPattern + "(\\w+)" + pathSeparatorPattern + "(\\d+)" + pathSeparatorPattern + "(\\w+)\\.xml";
			Pattern pattern = Pattern.compile(format);
			Matcher matcher = pattern.matcher(zipEntryName);
			if ( matcher.matches() ) {
				String rootEntity = matcher.group(1);
				String stepNumStr = matcher.group(2);
				String recordIdStr = matcher.group(3);
				int stepNumber = Integer.parseInt(stepNumStr);
				Step step = Step.valueOf(stepNumber);
				int recordId = Integer.parseInt(recordIdStr);
				BackupRecordEntry result = new BackupRecordEntry(rootEntity, step, recordId);
				return result;
			} else {
				throw new DataParsingExeption("Packaged file format exception: wrong zip entry name: " + zipEntryName + " expected: " + basePath + "...");
			}
		}
	
		public String getName() {
			return SurveyBackupJob.DATA_FOLDER + 
					SurveyBackupJob.ZIP_FOLDER_SEPARATOR + 
					rootEntity +
					SurveyBackupJob.ZIP_FOLDER_SEPARATOR + 
					step.getStepNumber() + 
					SurveyBackupJob.ZIP_FOLDER_SEPARATOR + 
					recordId + 
					".xml";
		}
		
		public String getRootEntity() {
			return rootEntity;
		}
		
		public int getRecordId() {
			return recordId;
		}
		
		public Step getStep() {
			return step;
		}
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + recordId;
			result = prime * result
					+ ((rootEntity == null) ? 0 : rootEntity.hashCode());
			result = prime * result + ((step == null) ? 0 : step.hashCode());
			return result;
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BackupRecordEntry other = (BackupRecordEntry) obj;
			if (recordId != other.recordId)
				return false;
			if (rootEntity == null) {
				if (other.rootEntity != null)
					return false;
			} else if (!rootEntity.equals(other.rootEntity))
				return false;
			if (step != other.step)
				return false;
			return true;
		}
	
	}
}
