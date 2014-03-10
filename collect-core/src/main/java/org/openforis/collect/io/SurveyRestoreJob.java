/**
 * 
 */
package org.openforis.collect.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.io.data.DataRestoreTask;
import org.openforis.collect.io.data.RecordFileRestoreTask;
import org.openforis.collect.io.metadata.IdmlImportTask;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignImportTask;
import org.openforis.collect.io.metadata.species.SpeciesBackupImportTask;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SurveyRestoreJob extends Job {

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private RecordFileManager recordFileManager;
	@Autowired
	private UserManager userManager;
	
	//parameters
	private transient File file;
	private String publishedSurveyUri;
	private boolean restoreData;
	private boolean updatingExistingSurvey;
	private boolean updatingPublishedSurvey;
	private String surveyName;

	//temporary instance variables
	private transient ZipFile zipFile;
	
	private transient CollectSurvey survey;
	
	@Override
	public void initInternal() throws Throwable {
		zipFile = new ZipFile(file);
		super.initInternal();
	}
	
	@Override
	protected void buildAndAddTasks() throws Throwable {
		addTask(IdmlImportTask.class);
		addTask(SamplingDesignImportTask.class);
		addSpeciesImportTasks();
		if ( updatingPublishedSurvey && restoreData && isDataIncluded() ) {
			addTask(DataRestoreTask.class);
			if ( isUploadedFilesIncluded() ) {
				addTask(RecordFileRestoreTask.class);
			}
		}
	}
	
	@Override
	protected void prepareTask(Task task) {
		if ( task instanceof IdmlImportTask ) {
			IdmlImportTask t = (IdmlImportTask) task;
			BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
			File idmlFile = backupFileExtractor.extractIdmlFile();
			t.setFile(idmlFile);
			t.setPublishedSurveyUri(publishedSurveyUri);
			t.setName(surveyName);
			t.setUpdatingExistingSurvey(updatingExistingSurvey);
			t.setUpdatingPublishedSurvey(updatingPublishedSurvey);
		} else if ( task instanceof SamplingDesignImportTask ) {
			SamplingDesignImportTask t = (SamplingDesignImportTask) task;
			BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
			File samplingDesignFile = backupFileExtractor.extract(SurveyBackupJob.SAMPLING_DESIGN_ENTRY_NAME);
			t.setFile(samplingDesignFile);
			t.setOverwriteAll(true);
			t.setSurvey(survey);
		} else if ( task instanceof SpeciesBackupImportTask ) {
			SpeciesBackupImportTask t = (SpeciesBackupImportTask) task;
			t.setSurvey(survey);
		} else if ( task instanceof DataRestoreTask ) {
			DataRestoreTask t = (DataRestoreTask) task;
			t.setRecordManager(recordManager);
			t.setUserManager(userManager);
			t.setEntryBasePath(SurveyBackupJob.DATA_FOLDER);
			t.setZipFile(zipFile);
			t.setPackagedSurvey(survey);
			t.setExistingSurvey(survey);
			t.setOverwriteAll(true);
		} else if ( task instanceof RecordFileRestoreTask ) {
			RecordFileRestoreTask t = (RecordFileRestoreTask) task;
			t.setRecordManager(recordManager);
			t.setRecordFileManager(recordFileManager);
			t.setZipFile(zipFile);
			t.setOverwriteAll(true);
			t.setSurvey(survey);
		}
		super.prepareTask(task);
	}
	
	@Override
	protected void onTaskCompleted(Task task) {
		super.onTaskCompleted(task);
		if ( task instanceof IdmlImportTask ) {
			IdmlImportTask t = (IdmlImportTask) task;
			//get output survey and set it into job instance instance variable
			SurveyRestoreJob.this.survey = t.getSurvey();
		}
	}
	
	private void addSpeciesImportTasks() {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
		List<String> speciesFilesNames = backupFileExtractor.listEntriesInPath(SurveyBackupJob.SPECIES_FOLDER);
		for (String speciesFileName : speciesFilesNames) {
			final SpeciesBackupImportTask task = createTask(SpeciesBackupImportTask.class);
			File file = backupFileExtractor.extract(speciesFileName);
			String taxonomyName = FilenameUtils.getBaseName(speciesFileName);
			task.setFile(file);
			task.setTaxonomyName(taxonomyName);
			task.setOverwriteAll(true);
			addTask(task);
		}
	}

	private boolean isDataIncluded() throws IOException {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
		List<String> dataEntries = backupFileExtractor.listEntriesInPath(SurveyBackupJob.DATA_FOLDER);
		return ! dataEntries.isEmpty();
	}

	private boolean isUploadedFilesIncluded() throws IOException {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
		List<String> dataEntries = backupFileExtractor.listEntriesInPath(SurveyBackupJob.UPLOADED_FILES_FOLDER);
		return ! dataEntries.isEmpty();
	}
	
	@Override
	protected void execute() throws Throwable {
		super.execute();
		if ( zipFile != null ) {
			zipFile.close();
		}
	}
	
	public static class BackupFileExtractor {
	
		private ZipFile zipFile;

		public BackupFileExtractor(ZipFile zipFile) {
			this.zipFile = zipFile;
		}
		
		public File extractIdmlFile() {
			return extract(SurveyBackupJob.SURVEY_XML_ENTRY_NAME);
		}
		
		public File extract(String entryName) {
			ZipEntry entry = findEntry(entryName);
			if ( entry == null ) {
				return null;
			} else {
				return extract(entry);
			}
		}

		private File extract(ZipEntry entry) {
			String entryName = entry.getName();
			try {
				InputStream is = zipFile.getInputStream(entry);
				String fileName = FilenameUtils.getName(entryName);
				File tempFile = File.createTempFile("collect", fileName);
				FileUtils.copyInputStreamToFile(is, tempFile);
				return tempFile;
			} catch (IOException e) {
				throw new RuntimeException(String.format("Error extracting file %s from backup archive: %s", entryName, e.getMessage()), e);
			}
		}
		
		public List<String> listEntriesInPath(String path) {
			if ( ! path.endsWith("/") ) {
				path += "/";
			}
			List<String> result = new ArrayList<String>();
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while ( zipEntries.hasMoreElements() ) {
				ZipEntry zipEntry = zipEntries.nextElement();
				String name = zipEntry.getName();
				if ( name.startsWith(path) ) {
					result.add(name);
				}
			}
			return result;
		}
		
		public List<File> extractFilesInPath(String folder) throws IOException {
			List<File> result = new ArrayList<File>();
			List<String> entryNames = listEntriesInPath(folder);
			for (String name : entryNames) {
				File tempFile = extract(name);
				result.add(tempFile);
			}
			return result;
		}
		
		public ZipEntry findEntry(String entryName) {
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while ( zipEntries.hasMoreElements() ) {
				ZipEntry zipEntry = zipEntries.nextElement();
				String name = zipEntry.getName();
				if ( ! zipEntry.isDirectory() && name.equals(entryName)  ) {
					return zipEntry;
				}
			}
			return null;
		}
		
		public InputStream findEntryInputStream(String entryName) throws IOException {
			ZipEntry entry = findEntry(entryName);
			if ( entry == null ) {
				return null;
			} else {
				InputStream is = zipFile.getInputStream(entry);
				return is;
			}
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}

	public boolean isUpdatingExistingSurvey() {
		return updatingExistingSurvey;
	}

	public void setUpdatingExistingSurvey(boolean updatingExistingSurvey) {
		this.updatingExistingSurvey = updatingExistingSurvey;
	}

	public boolean isUpdatingPublishedSurvey() {
		return updatingPublishedSurvey;
	}

	public void setUpdatingPublishedSurvey(boolean updatingPublishedSurvey) {
		this.updatingPublishedSurvey = updatingPublishedSurvey;
	}

	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

	public String getPublishedSurveyUri() {
		return publishedSurveyUri;
	}

	public void setPublishedSurveyUri(String publishedSurveyUri) {
		this.publishedSurveyUri = publishedSurveyUri;
	}
}
