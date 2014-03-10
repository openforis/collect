package org.openforis.collect.io.data;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyRestoreJob;
import org.openforis.collect.io.SurveyRestoreJob.BackupFileExtractor;
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

	public BackupDataExtractor(CollectSurvey survey, File file) throws ZipException, IOException {
		this(survey, new ZipFile(file));
	}
	
	public BackupDataExtractor(CollectSurvey survey, ZipFile zipFile) {
		this.survey = survey;
		this.zipFile = zipFile;
		this.initialized = false;
		this.fileExtractor = new SurveyRestoreJob.BackupFileExtractor(zipFile);
	}

	public void init() throws ZipException, IOException {
		this.dataUnmarshaller = new DataUnmarshaller(new DataHandler(survey));
		this.zipEntries = zipFile.entries();
		this.initialized = true;
	}

	public ParseRecordResult nextRecord(Step step) throws Exception {
		checkInitialized();
		ParseRecordResult result = null;
		ZipEntry zipEntry = nextDataEntry();
		while ( zipEntry != null ) {
			String entryName = zipEntry.getName();
			if ( BackupRecordEntry.isValidDataEntry(zipEntry) ) {
				Step entryStep = extractStep(entryName);
				if ( step == null || step == entryStep ) {
					InputStream is = zipFile.getInputStream(zipEntry);
					return parse(is);
				} else {
					zipEntry = nextDataEntry();
				}
			}
		}
		return result;
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
			if ( BackupRecordEntry.isValidDataEntry(zipEntry) ) {
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
			throw new IllegalStateException("Exctractor not inited");
		}
	}
	
	private Step extractStep(String zipEntryName) throws BackupDataExtractorException {
		String[] entryNameSplitted = getEntryNameSplitted(zipEntryName);
		String stepNumStr = entryNameSplitted[0];
		int stepNumber = Integer.parseInt(stepNumStr);
		return Step.valueOf(stepNumber);
	}
	
	private String[] getEntryNameSplitted(String zipEntryName) throws BackupDataExtractorException {
		String entryPathSeparator = Pattern.quote(File.separator);
		String[] entryNameSplitted = zipEntryName.split(entryPathSeparator);
		if (entryNameSplitted.length != 2) {
			entryPathSeparator = Pattern.quote("/");
			entryNameSplitted = zipEntryName.split(entryPathSeparator);
		}
		if (entryNameSplitted.length != 2) {
			throw new BackupDataExtractorException("Packaged file format exception: wrong entry name: " + zipEntryName);
		}
		return entryNameSplitted;
	}

	public long countRecords(Step step) throws BackupDataExtractorException {
		checkInitialized();
		int count = 0;
		ZipEntry entry = nextDataEntry();
		while ( entry != null ) {
			String entryName = entry.getName();
			Step entryStep = extractStep(entryName);
			if ( step == null || step == entryStep ) {
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
		
		public static boolean isValidDataEntry(ZipEntry zipEntry) {
			return isValidRecordEntry(zipEntry, SurveyBackupJob.DATA_FOLDER);
		}
		
		public static boolean isValidRecordEntry(ZipEntry zipEntry, String basePath) {
			if ( ! basePath.endsWith("/") ) {
				basePath += "/";
			}
			String name = zipEntry.getName();
			return ! zipEntry.isDirectory() && name.startsWith(basePath);
		}
		
		public static BackupRecordEntry parse(String zipEntryName, String basePath) throws DataParsingExeption {
			//for backward compatibility with previous generated backup files
			String zipEntryNameFixed = zipEntryName.replace("\\", SurveyBackupJob.ZIP_FOLDER_SEPARATOR);
			if ( basePath != null ) {
				if ( ! basePath.endsWith("/") ) {
					basePath += "/";
				}
				if ( zipEntryName.startsWith(basePath) ) {
					zipEntryNameFixed = zipEntryNameFixed.substring(basePath.length());
				} else {
					throw new DataParsingExeption("Packaged file format exception: wrong zip entry name: " + zipEntryName + " expected: " + basePath + "...");
				}
			}
			String[] entryNameSplitted = zipEntryNameFixed.split(SurveyBackupJob.ZIP_FOLDER_SEPARATOR);
			if (entryNameSplitted.length != 3) {
				throw new DataParsingExeption("Packaged file format exception: wrong zip entry name: " + zipEntryName);
			}
			//root entity
			String rootEntity = entryNameSplitted[0];
			//step
			String stepNumStr = entryNameSplitted[1];
			int stepNumber = Integer.parseInt(stepNumStr);
			Step step = Step.valueOf(stepNumber);
			//file name
			String fileName = entryNameSplitted[2];
			String baseName = FilenameUtils.getBaseName(fileName);
			int recordId = Integer.parseInt(baseName);
			BackupRecordEntry result = new BackupRecordEntry(rootEntity, step, recordId);
			return result;
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
