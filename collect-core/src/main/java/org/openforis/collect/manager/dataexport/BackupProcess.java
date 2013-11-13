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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.DataExportStatus.Format;
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

	private static final String FILE_NAME = "data.zip";
	private static final String ZIP_DIRECTORY_SEPARATOR = "/";
	public static final String RECORD_FILE_DIRECTORY_NAME = "upload";

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
				String fileName = FILE_NAME;
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
		String entryFileName = "idml.xml";
		ZipEntry entry = new ZipEntry(entryFileName);
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
			String entryFileName = buildEntryFileName(record, step.getStepNumber());
			ZipEntry entry = new ZipEntry(entryFileName);
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
			CollectRecord record) {
		List<FileAttribute> fileAttributes = record.getFileAttributes();
		for (FileAttribute fileAttribute : fileAttributes) {
			File file = recordFileManager.getRepositoryFile(record, fileAttribute.getInternalId());
			FileAttributeDefinition fileAttributeDefinition = fileAttribute.getDefinition();
			String repositoryRelativePath = recordFileManager.getRepositoryRelativePath(fileAttributeDefinition, ZIP_DIRECTORY_SEPARATOR, false);
			String relativePath = RECORD_FILE_DIRECTORY_NAME + ZIP_DIRECTORY_SEPARATOR + repositoryRelativePath;
			String fileName = relativePath + ZIP_DIRECTORY_SEPARATOR + fileAttribute.getFilename();
			writeFile(zipOutputStream, file, fileName);
		}
	}

	private void writeFile(ZipOutputStream zipOutputStream, File file, String fileName) {
		try {
			ZipEntry entry = new ZipEntry(fileName);
			zipOutputStream.putNextEntry(entry);
			IOUtils.copy(new FileInputStream(file), zipOutputStream);
			zipOutputStream.closeEntry();
			zipOutputStream.flush();
		} catch (IOException e) {
			LOG.error(String.format("Error writing record file (fileName: %s)", fileName));
		}
	}

	private String buildEntryFileName(CollectRecord record, int stepNumber) {
		return stepNumber + ZIP_DIRECTORY_SEPARATOR + record.getId() + ".xml";
	}

	public boolean isIncludeIdm() {
		return includeIdm;
	}

	public void setIncludeIdm(boolean includeIdm) {
		this.includeIdm = includeIdm;
	}
	
}
