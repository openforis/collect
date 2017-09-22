package org.openforis.collect.io.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.exception.RecordFileException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVerifier;
import org.openforis.idm.model.FileAttribute;
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
	
	private transient Log log = LogFactory.getLog(RecordFileBackupTask.class);

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
		if (hasFileAttributeDefinitions()) {
			List<CollectRecordSummary> recordSummaries = loadAllSummaries();
			return recordSummaries.size();
		} else {
			return 0;
		}
	}
	
	@Override
	protected void execute() throws Throwable {
		if (! hasFileAttributeDefinitions()) {
			return;
		}
		List<CollectRecordSummary> recordSummaries = loadAllSummaries();
		if ( recordSummaries != null ) {
			for (CollectRecordSummary summary : recordSummaries) {
				if ( isRunning() ) {
					try {
						backup(summary);
						incrementProcessedItems();
					} catch(Exception e) {
						log.error(String.format("Error backing up record files for record with id %d and keys %s", 
								summary.getId(), summary.getRootEntityKeyValues().toString()));
					}
				} else {
					break;
				}
			}
		}
	}

	private List<CollectRecordSummary> loadAllSummaries() {
		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(survey.getSchema().getRootEntityDefinition(rootEntityName).getId());
		List<CollectRecordSummary> summaries = recordManager.loadSummaries(filter);
		return summaries;
	}
	
	private boolean hasFileAttributeDefinitions() {
		@SuppressWarnings("unchecked")
		List<FileAttributeDefinition> defs = (List<FileAttributeDefinition>) 
				survey.getSchema().findNodeDefinitions(new NodeDefinitionVerifier() {
			@Override
			public boolean verify(NodeDefinition definition) {
				return definition instanceof FileAttributeDefinition;
			}
		});
		return ! defs.isEmpty();
	}
	
	private void backup(CollectRecordSummary summary) throws RecordFileException, IOException {
		Integer id = summary.getId();
		CollectRecord record = recordManager.load(survey, id, summary.getStep(), false);
		List<FileAttribute> fileAttributes = record.getFileAttributes();
		for (FileAttribute fileAttribute : fileAttributes) {
			if ( ! fileAttribute.isEmpty() ) {
				File file = recordFileManager.getRepositoryFile(fileAttribute);
				if ( file == null || ! file.exists() ) {
					log.error(String.format("Record file not found for record %s (%d) attribute %s (%d)", 
							StringUtils.join(record.getRootEntityKeyValues(), ','), record.getId(), fileAttribute.getPath(), fileAttribute.getInternalId()));
					//throw new RecordFileException(message);
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
