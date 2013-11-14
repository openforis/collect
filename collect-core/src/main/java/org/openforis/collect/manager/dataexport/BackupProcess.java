package org.openforis.collect.manager.dataexport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.DataExportStatus.Format;
import org.openforis.collect.manager.exception.DataImportExeption;
import org.openforis.collect.manager.exception.RecordFileException;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.model.FileAttribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class BackupProcess extends AbstractProcess<Void, DataExportStatus> {


	private static Log LOG = LogFactory.getLog(BackupProcess.class);

	public static final String IDML_FILE_NAME = "idml.xml";
	public static final String ZIP_DIRECTORY_SEPARATOR = "/";
	public static final String RECORD_FILE_DIRECTORY_NAME = "upload";

	private static final String OUTPUT_FILE_NAME = "data.zip";

	private RecordManager recordManager;
	private RecordFileManager recordFileManager;
	private SurveyManager surveyManager;
	private DataMarshaller dataMarshaller;
	
	private File directory;
	private CollectSurvey survey;
	private int[] stepNumbers;
	private String rootEntityName;

	private boolean includeIdm;
	
	public BackupProcess(SurveyManager surveyManager, RecordManager recordManager,
			RecordFileManager recordFileManager,
			DataMarshaller dataMarshaller, File directory,
			CollectSurvey survey, String rootEntityName, int[] stepNumbers) {
		super();
		this.surveyManager = surveyManager;
		this.recordManager = recordManager;
		this.recordFileManager = recordFileManager;
		this.dataMarshaller = dataMarshaller;
		this.directory = directory;
		this.survey = survey;
		this.rootEntityName = rootEntityName;
		this.stepNumbers = stepNumbers;
		this.includeIdm = true;
	}

	@Override
	protected void initStatus() {
		this.status = new DataExportStatus(Format.XML);
	}
	
	@Override
	public void startProcessing() throws Exception {
		super.startProcessing();
		try {
			List<CollectRecord> recordSummaries = loadAllSummaries();
			if ( recordSummaries != null && stepNumbers != null ) {
				String fileName = OUTPUT_FILE_NAME;
				File file = new File(directory, fileName);
				if (file.exists()) {
					file.delete();
				}
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
				ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);
				backup(zipOutputStream, recordSummaries);
				zipOutputStream.flush();
				zipOutputStream.close();
			}
		} catch (Exception e) {
			status.error();
			LOG.error("Error during data export", e);
		}
	}

	private void backup(ZipOutputStream zipOutputStream, List<CollectRecord> recordSummaries) {
		int total = calculateTotal(recordSummaries);
		status.setTotal(total);
		if ( includeIdm ) {
			includeIdml(zipOutputStream);
		}
		for (CollectRecord summary : recordSummaries) {
			if ( status.isRunning() ) {
				int recordStepNumber = summary.getStep().getStepNumber();
				for (int stepNum: stepNumbers) {
					if ( stepNum <= recordStepNumber) {
						backup(zipOutputStream, summary, Step.valueOf(stepNum));
						status.incrementProcessed();
					}
				}
			} else {
				break;
			}
		}
	}

	private List<CollectRecord> loadAllSummaries() {
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName, 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, (String) null);
		return summaries;
	}
	
	private int calculateTotal(List<CollectRecord> recordSummaries) {
		int count = 0;
		for (CollectRecord summary : recordSummaries) {
			int recordStepNumber = summary.getStep().getStepNumber();
			for (int stepNumber: stepNumbers) {
				if ( stepNumber <= recordStepNumber ) {
					count ++;
				}
			}
		}
		return count;
	}
	
	private void includeIdml(ZipOutputStream zipOutputStream) {
		ZipEntry entry = new ZipEntry(IDML_FILE_NAME);
		try {
			zipOutputStream.putNextEntry(entry);
			surveyManager.marshalSurvey(survey, zipOutputStream, true, true, false);
//			String surveyMarshalled = surveyManager.marshalSurvey(survey);
//			PrintWriter printWriter = new PrintWriter(zipOutputStream);
//			printWriter.write(surveyMarshalled);
			zipOutputStream.closeEntry();
			zipOutputStream.flush();
		} catch (IOException e) {
			String message = "Error while including idml into zip file: " + e.getMessage();
			if (LOG.isErrorEnabled()) {
				LOG.error(message, e);
			}
			throw new RuntimeException(message, e);
		}
	}
	
	private void backup(ZipOutputStream zipOutputStream, CollectRecord summary, Step step) {
		Integer id = summary.getId();
		try {
			CollectRecord record = recordManager.load(survey, id, step);
			RecordEntry recordEntry = new RecordEntry(step, id);
			String entryName = recordEntry.getName();
			ZipEntry entry = new ZipEntry(entryName);
			zipOutputStream.putNextEntry(entry);
			OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream);
			dataMarshaller.write(record, writer);
			zipOutputStream.closeEntry();
			zipOutputStream.flush();
			backupRecordFiles(zipOutputStream, record);
		} catch (Exception e) {
			String message = "Error while backing up " + id + " " + e.getMessage();
			if (LOG.isErrorEnabled()) {
				LOG.error(message, e);
			}
			throw new RuntimeException(message, e);
		}
	}
	
	private void backupRecordFiles(ZipOutputStream zipOutputStream,
			CollectRecord record) throws RecordFileException {
		List<FileAttribute> fileAttributes = record.getFileAttributes();
		for (FileAttribute fileAttribute : fileAttributes) {
			if ( ! fileAttribute.isEmpty() ) {
				File file = recordFileManager.getRepositoryFile(fileAttribute);
				if ( file == null ) {
					String message = String.format("Missing file: %s attributeId: %d attributeName: %s", 
							fileAttribute.getFilename(), fileAttribute.getInternalId(), fileAttribute.getName());
					throw new RecordFileException(message);
				} else {
					String entryName = calculateRecordFileEntryName(fileAttribute);
					writeFile(zipOutputStream, file, entryName);
				}
			}
		}
	}

	public static String calculateRecordFileEntryName(FileAttribute fileAttribute) {
		FileAttributeDefinition fileAttributeDefinition = fileAttribute.getDefinition();
		String repositoryRelativePath = RecordFileManager.getRepositoryRelativePath(fileAttributeDefinition, ZIP_DIRECTORY_SEPARATOR, false);
		String relativePath = RECORD_FILE_DIRECTORY_NAME + ZIP_DIRECTORY_SEPARATOR + repositoryRelativePath;
		String entryName = relativePath + ZIP_DIRECTORY_SEPARATOR + fileAttribute.getFilename();
		return entryName;
	}

	private void writeFile(ZipOutputStream zipOutputStream, File file, String entryName) {
		try {
			ZipEntry entry = new ZipEntry(entryName);
			zipOutputStream.putNextEntry(entry);
			IOUtils.copy(new FileInputStream(file), zipOutputStream);
			zipOutputStream.closeEntry();
			zipOutputStream.flush();
		} catch (IOException e) {
			LOG.error(String.format("Error writing record file (fileName: %s)", entryName));
		}
	}

	public boolean isIncludeIdm() {
		return includeIdm;
	}

	public void setIncludeIdm(boolean includeIdm) {
		this.includeIdm = includeIdm;
	}
	
	public static class RecordEntry {
		private Step step;
		private int recordId;
		
		public RecordEntry(Step step, int recordId) {
			this.step = step;
			this.recordId = recordId;
		}
		
		public static boolean isValidRecordEntry(ZipEntry zipEntry) {
			String name = zipEntry.getName();
			return ! (zipEntry.isDirectory() || IDML_FILE_NAME.equals(name) || 
					name.startsWith(RECORD_FILE_DIRECTORY_NAME));
		}
		
		public static RecordEntry parse(String zipEntryName) throws DataImportExeption {
			//for retro compatibility with previous generated backup files
			String zipEntryNameFixed = zipEntryName.replace("\\", ZIP_DIRECTORY_SEPARATOR);
			String[] entryNameSplitted = zipEntryNameFixed.split(ZIP_DIRECTORY_SEPARATOR);
			if (entryNameSplitted.length != 2) {
				throw new DataImportExeption("Packaged file format exception: wrong zip entry name: " + zipEntryName);
			}
			//step
			String stepNumStr = entryNameSplitted[0];
			int stepNumber = Integer.parseInt(stepNumStr);
			Step step = Step.valueOf(stepNumber);
			//file name
			String fileName = entryNameSplitted[1];
			String baseName = FilenameUtils.getBaseName(fileName);
			int recordId = Integer.parseInt(baseName);
			RecordEntry result = new RecordEntry(step, recordId);
			return result;
		}

		public String getName() {
			return step.getStepNumber() + ZIP_DIRECTORY_SEPARATOR + recordId + ".xml";
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
			RecordEntry other = (RecordEntry) obj;
			if (recordId != other.recordId)
				return false;
			if (step != other.step)
				return false;
			return true;
		}
		
	}
	
}
