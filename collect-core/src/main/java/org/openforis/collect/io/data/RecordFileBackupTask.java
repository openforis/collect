package org.openforis.collect.io.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.RecordFileException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.model.FileAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RecordFileBackupTask extends Task {

	private RecordManager recordManager;
	private RecordFileManager recordFileManager;

	//input
	private ZipOutputStream zipOutputStream;
	private CollectSurvey survey;
	private String rootEntityName;
	
	public RecordFileBackupTask() {
		super();
	}

	@Override
	protected long countTotalItems() {
		List<CollectRecord> recordSummaries = loadAllSummaries();
		return recordSummaries.size();
	}
	
	@Override
	protected void execute() throws Throwable {
		List<CollectRecord> recordSummaries = loadAllSummaries();
		if ( recordSummaries != null ) {
			for (CollectRecord summary : recordSummaries) {
				if ( isRunning() ) {
					backup(summary);
					incrementItemsProcessed();
				} else {
					break;
				}
			}
		}
	}

	private List<CollectRecord> loadAllSummaries() {
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName);
		return summaries;
	}
	
	private void backup(CollectRecord summary) throws RecordFileException, IOException {
		Integer id = summary.getId();
		CollectRecord record = recordManager.load(survey, id, summary.getStep());
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
					writeFile(file, entryName);
				}
			}
		}
	}

	public static String calculateRecordFileEntryName(FileAttribute fileAttribute) {
		FileAttributeDefinition fileAttributeDefinition = fileAttribute.getDefinition();
		String repositoryRelativePath = RecordFileManager.getRepositoryRelativePath(fileAttributeDefinition, SurveyBackupJob.ZIP_FOLDER_SEPARATOR, false);
		String entryName = SurveyBackupJob.UPLOADED_FILES_FOLDER + SurveyBackupJob.ZIP_FOLDER_SEPARATOR + repositoryRelativePath + SurveyBackupJob.ZIP_FOLDER_SEPARATOR + fileAttribute.getFilename();
		return entryName;
	}

	private void writeFile(File file, String entryName) throws IOException {
		ZipEntry entry = new ZipEntry(entryName);
		zipOutputStream.putNextEntry(entry);
		IOUtils.copy(new FileInputStream(file), zipOutputStream);
		zipOutputStream.closeEntry();
		zipOutputStream.flush();
	}

	public RecordManager getRecordManager() {
		return recordManager;
	}
	
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public RecordFileManager getRecordFileManager() {
		return recordFileManager;
	}
	
	public void setRecordFileManager(RecordFileManager recordFileManager) {
		this.recordFileManager = recordFileManager;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public String getRootEntityName() {
		return rootEntityName;
	}
	
	public void setRootEntityName(String rootEntityName) {
		this.rootEntityName = rootEntityName;
	}
	
	public ZipOutputStream getZipOutputStream() {
		return zipOutputStream;
	}

	public void setZipOutputStream(ZipOutputStream zipOutputStream) {
		this.zipOutputStream = zipOutputStream;
	}

}
