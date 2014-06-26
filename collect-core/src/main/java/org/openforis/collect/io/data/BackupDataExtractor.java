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

import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.exception.DataParsingExeption;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.commons.io.OpenForisIOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class BackupDataExtractor implements Closeable {

	protected static final String IDML_FILE_NAME = "idml.xml";

	//params
	protected ZipFile zipFile;
	private CollectSurvey survey;
	private Step step;
	
	//transient
	private boolean initialized;
	protected Enumeration<? extends ZipEntry> zipEntries;
	private BackupFileExtractor fileExtractor;
	private DataUnmarshaller dataUnmarshaller;
	private boolean oldFormat;

	public BackupDataExtractor(CollectSurvey survey, ZipFile zipFile, Step step) {
		this.survey = survey;
		this.zipFile = zipFile;
		this.initialized = false;
		this.step = step;
	}

	public BackupDataExtractor(CollectSurvey survey, File file, Step step) throws ZipException, IOException {
		this(survey, new ZipFile(file), step);
	}
	
	public void init() throws ZipException, IOException {
		this.fileExtractor = new BackupFileExtractor(zipFile);
		this.oldFormat = this.fileExtractor.isOldFormat();
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
			if ( BackupRecordEntry.isValidRecordEntry(zipEntry, oldFormat) ) {
				BackupRecordEntry recordEntry = BackupRecordEntry.parse(entryName, oldFormat);
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
		return (step == null || step == recordEntry.getStep());
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
			if ( BackupRecordEntry.isValidRecordEntry(zipEntry, oldFormat) ) {
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
			BackupRecordEntry recordEntry = BackupRecordEntry.parse(entryName, oldFormat);
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
		
		private Step step;
		private int recordId;
		private boolean oldEntryFormat;
		
		private static final String pathSeparatorsPatern;
		static {
			String[] pathSeparators = new String[] {"\\", "/"}; //for backwards compatibility, allow both \ and / as path separators
			String pattern = "[";
			for (String separator : pathSeparators) {
				pattern += Pattern.quote(separator);
			}
			pattern += "]";
			pathSeparatorsPatern = pattern;
		}
		
		private static Pattern OLD_ENTRY_PATTERN = Pattern.compile("(\\d+)" + pathSeparatorsPatern + "(\\w+).xml");
		private static Pattern ENTRY_PATTERN =  Pattern.compile(Pattern.quote(SurveyBackupJob.DATA_FOLDER) + "/(\\d+)/(\\w+).xml");
		
		public BackupRecordEntry(Step step, int recordId) {
			this(step, recordId, false);
		}
		
		public BackupRecordEntry(Step step, int recordId, boolean oldEntryFormat) {
			this.step = step;
			this.recordId = recordId;
			this.oldEntryFormat = oldEntryFormat;
		}

		public static boolean isValidRecordEntry(ZipEntry zipEntry) {
			return isValidRecordEntry(zipEntry, false);
		}
		
		public static boolean isValidRecordEntry(ZipEntry zipEntry, boolean oldFormat) {
			String entryName = zipEntry.getName();
			Pattern entryPattern = oldFormat ? OLD_ENTRY_PATTERN: ENTRY_PATTERN;
			return ! zipEntry.isDirectory() && entryPattern.matcher(entryName).matches();
		}
		
		public static BackupRecordEntry parse(String zipEntryName) throws DataParsingExeption {
			return parse(zipEntryName, false);
		}
		
		public static BackupRecordEntry parse(String zipEntryName, boolean oldFormat) throws DataParsingExeption {
			if ( oldFormat ) {
				return parseOldFormat(zipEntryName);
			} else {
				return parseNewFormat(zipEntryName);
			}
		}
		
		public static BackupRecordEntry parseOldFormat(String zipEntryName) throws DataParsingExeption {
			Matcher matcher = OLD_ENTRY_PATTERN.matcher(zipEntryName);
			if ( matcher.matches() ) {
				String stepNumStr = matcher.group(1);
				String recordIdStr = matcher.group(2);
				int stepNumber = Integer.parseInt(stepNumStr);
				Step step = Step.valueOf(stepNumber);
				int recordId = Integer.parseInt(recordIdStr);
				BackupRecordEntry result = new BackupRecordEntry(step, recordId);
				return result;
			} else {
				throw new DataParsingExeption("Packaged file format exception: wrong zip entry name: " + zipEntryName + " expected: " + "STEP_NO/RECORD_ID.xml");
			}
		}
		
		private static BackupRecordEntry parseNewFormat(String zipEntryName) throws DataParsingExeption {
			Matcher matcher = ENTRY_PATTERN.matcher(zipEntryName);
			if ( matcher.matches() ) {
				String stepNumStr = matcher.group(1);
				String recordIdStr = matcher.group(2);
				int stepNumber = Integer.parseInt(stepNumStr);
				Step step = Step.valueOf(stepNumber);
				int recordId = Integer.parseInt(recordIdStr);
				BackupRecordEntry result = new BackupRecordEntry(step, recordId);
				return result;
			} else {
				throw new DataParsingExeption("Packaged file format exception: wrong zip entry name: " + zipEntryName + " expected: " + 
						SurveyBackupJob.DATA_FOLDER + SurveyBackupJob.ZIP_FOLDER_SEPARATOR + "...");
			}
		}
	
		public String getName() {
			return (oldEntryFormat ? "": SurveyBackupJob.DATA_FOLDER + SurveyBackupJob.ZIP_FOLDER_SEPARATOR) + 
					step.getStepNumber() + 
					SurveyBackupJob.ZIP_FOLDER_SEPARATOR + 
					recordId + ".xml";
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
			if (step != other.step)
				return false;
			return true;
		}
	
	}
}
