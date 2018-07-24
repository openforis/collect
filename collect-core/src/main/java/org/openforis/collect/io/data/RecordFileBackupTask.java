package org.openforis.collect.io.data;

import static org.openforis.collect.io.SurveyBackupJob.UPLOADED_FILES_FOLDER;
import static org.openforis.collect.io.SurveyBackupJob.ZIP_FOLDER_SEPARATOR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.exception.RecordFileException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.commons.collection.Predicate;
import org.openforis.commons.collection.Visitor;
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
	
	private static final transient Logger LOG = LogManager.getLogger(RecordFileBackupTask.class);

	private RecordManager recordManager;
	private RecordFileManager recordFileManager;

	//input
	private ZipOutputStream zipOutputStream;
	private CollectSurvey survey;
	private String rootEntityName;
	
	//output
	private List<CollectRecordSummary> skippedRecords = new ArrayList<CollectRecordSummary>();
	private List<MissingRecordFileError> missingRecordFiles = new ArrayList<MissingRecordFileError>();
	
	@Override
	protected long countTotalItems() {
		if (hasFileAttributeDefinitions()) {
			RecordFilter filter = new RecordFilter(survey);
			filter.setRootEntityId(survey.getSchema().getRootEntityDefinition(rootEntityName).getId());
			int count = recordManager.countRecords(filter);
			return count;
		} else {
			return 0;
		}
	}
	
	@Override
	protected void execute() throws Throwable {
		if (! hasFileAttributeDefinitions()) {
			return;
		}
		
		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(survey.getSchema().getRootEntityDefinition(rootEntityName).getId());
		recordManager.visitSummaries(filter, new Visitor<CollectRecordSummary>() {
			public void visit(CollectRecordSummary summary) {
				try {
					backup(summary);
					incrementProcessedItems();
				} catch(Exception e) {
					addSkippedRecord(summary);
					LOG.error(String.format("Error backing up record files for record with id %d and keys %s", 
							summary.getId(), summary.getRootEntityKeyValues().toString()));
				}
			}
		}, new Predicate<CollectRecordSummary>() {
			public boolean evaluate(CollectRecordSummary s) {
				return !isRunning();
			}
		});
	}

	private void addSkippedRecord(CollectRecordSummary summary) {
		this.skippedRecords.add(summary);
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
			if ( StringUtils.isNotBlank(fileAttribute.getFilename()) ) {
				File file = recordFileManager.getRepositoryFile(fileAttribute);
				if ( file != null && file.exists() ) {
					String entryName = determineRecordFileEntryName(fileAttribute);
					writeFile(file, entryName);
				} else {
					addSkippedFileError(summary, fileAttribute.getPath(), recordFileManager.getRepositoryFileAbsolutePath(fileAttribute));
					LOG.error(String.format("Record file not found for record %s (%d) attribute %s (%d)", 
							StringUtils.join(record.getRootEntityKeyValues(), ','), record.getId(), fileAttribute.getPath(), fileAttribute.getInternalId()));
					//throw new RecordFileException(message);
				}
			}
		}
	}

	private void addSkippedFileError(CollectRecordSummary summary, String fileAttributePath, String fileAbsolutePath) {
		this.missingRecordFiles.add(new MissingRecordFileError(summary, fileAttributePath, fileAbsolutePath));
	}

	public static String determineRecordFileEntryName(FileAttribute fileAttribute) {
		FileAttributeDefinition fileAttributeDefinition = fileAttribute.getDefinition();
		String repositoryRelativePath = RecordFileManager.getRepositoryRelativePath(fileAttributeDefinition, ZIP_FOLDER_SEPARATOR, false);
		String filename = fileAttribute.getFilename();
		return StringUtils.join(Arrays.asList(UPLOADED_FILES_FOLDER, repositoryRelativePath, filename), ZIP_FOLDER_SEPARATOR);
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
	
	public List<CollectRecordSummary> getSkippedRecords() {
		return skippedRecords;
	}
	
	public List<MissingRecordFileError> getMissingRecordFiles() {
		return missingRecordFiles;
	}
	
	public static class MissingRecordFileError {
		
		private CollectRecordSummary recordSummary;
		private String fileAttributePath;
		private String filePath;
		
		public MissingRecordFileError(CollectRecordSummary recordSummary, String fileAttributePath, String filePath) {
			super();
			this.recordSummary = recordSummary;
			this.fileAttributePath = fileAttributePath;
			this.filePath = filePath;
		}
		
		public CollectRecordSummary getRecordSummary() {
			return recordSummary;
		}
		
		public String getFileAttributePath() {
			return fileAttributePath;
		}
		
		public String getFilePath() {
			return filePath;
		}
	}
}
