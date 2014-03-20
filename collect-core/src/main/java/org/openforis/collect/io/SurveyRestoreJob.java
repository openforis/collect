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
import org.openforis.collect.io.internal.SurveyBackupInfoExtractorTask;
import org.openforis.collect.io.metadata.IdmlImportTask;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignImportTask;
import org.openforis.collect.io.metadata.species.SpeciesBackupImportTask;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
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
public class SurveyRestoreJob extends AbstractSurveyRestoreJob {

	@Autowired
	private transient SpeciesManager speciesManager;
	@Autowired
	private transient SamplingDesignManager samplingDesignManager;

	//output
	private SurveyBackupInfo backupInfo;

	//temporary instance variables
	private transient ZipFile zipFile;
	private BackupFileExtractor backupFileExtractor;
	
	@Override
	public void initInternal() throws Throwable {
		if ( isCompleteBackupFile() ) {
			this.zipFile = new ZipFile(file);
			this.backupFileExtractor = new BackupFileExtractor(zipFile);
			super.initInternal();
		} else {
			throw new IllegalArgumentException("File is not a valid survey backup ZIP file: " + file.getName());
		}
	}

	private boolean isCompleteBackupFile() {
		String ext = FilenameUtils.getExtension(file.getName());
		return "zip".equalsIgnoreCase(ext);
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		addTask(SurveyBackupInfoExtractorTask.class);
		if ( surveyUri == null ) {
			//unmarshall xml file to get survey uri
			addTask(IdmlUnmarshallTask.class);
		}
		
		addTask(IdmlImportTask.class);
		
		if ( backupFileExtractor.containsEntry(SurveyBackupJob.SAMPLING_DESIGN_ENTRY_NAME) ) {
			addTask(SamplingDesignImportTask.class);
		}
		if ( backupFileExtractor.containsEntriesInPath(SurveyBackupJob.SPECIES_FOLDER) ) {
			addSpeciesImportTasks();
		}
	}

	@Override
	protected void prepareTask(Task task) {
		if ( task instanceof SurveyBackupInfoExtractorTask ) {
			SurveyBackupInfoExtractorTask t = (SurveyBackupInfoExtractorTask) task;
			File infoFile = backupFileExtractor.extract(SurveyBackupJob.INFO_FILE_NAME);
			t.setFile(infoFile);
		} else if ( task instanceof IdmlUnmarshallTask ) {
			IdmlUnmarshallTask t = (IdmlUnmarshallTask) task;
			File idmlFile = backupFileExtractor.extractIdmlFile();
			t.setFile(idmlFile);
			t.setSurveyManager(surveyManager);
			t.setValidate(false);
		} else if ( task instanceof IdmlImportTask ) {
			IdmlImportTask t = (IdmlImportTask) task;
			t.setSurveyManager(surveyManager);
			File idmlFile = backupFileExtractor.extractIdmlFile();
			t.setFile(idmlFile);
			t.setSurveyUri(surveyUri);
			t.setSurveyName(surveyName);
			t.setImportInPublishedSurvey(restoreIntoPublishedSurvey);
			t.setValidate(validateSurvey);
		} else if ( task instanceof SamplingDesignImportTask ) {
			SamplingDesignImportTask t = (SamplingDesignImportTask) task;
			File samplingDesignFile = backupFileExtractor.extract(SurveyBackupJob.SAMPLING_DESIGN_ENTRY_NAME);
			t.setSamplingDesignManager(samplingDesignManager);
			t.setFile(samplingDesignFile);
			t.setOverwriteAll(true);
			t.setSurvey(survey);
		} else if ( task instanceof SpeciesBackupImportTask ) {
			SpeciesBackupImportTask t = (SpeciesBackupImportTask) task;
			t.setSpeciesManager(speciesManager);
			t.setSurvey(survey);
		}
		super.prepareTask(task);
	}
	
	@Override
	protected void onTaskCompleted(Task task) {
		super.onTaskCompleted(task);
		if ( task instanceof SurveyBackupInfoExtractorTask ) {
			SurveyBackupInfoExtractorTask t = (SurveyBackupInfoExtractorTask) task;
			this.backupInfo = t.getInfo();
		} else if ( task instanceof IdmlUnmarshallTask ) {
			CollectSurvey s = ((IdmlUnmarshallTask) task).getSurvey();
			this.surveyUri = s.getUri();
		} else if ( task instanceof IdmlImportTask ) {
			IdmlImportTask t = (IdmlImportTask) task;
			//get output survey and set it into job instance instance variable
			this.survey = t.getSurvey();
		}
	}
	
	private void addSpeciesImportTasks() {
		List<String> speciesFilesNames = backupFileExtractor.listEntriesInPath(SurveyBackupJob.SPECIES_FOLDER);
		for (String speciesFileName : speciesFilesNames) {
			String taxonomyName = FilenameUtils.getBaseName(speciesFileName);
			File file = backupFileExtractor.extract(speciesFileName);
			SpeciesBackupImportTask task = createTask(SpeciesBackupImportTask.class);
			task.setFile(file);
			task.setTaxonomyName(taxonomyName);
			task.setOverwriteAll(true);
			addTask(task);
		}
	}
	
	@Override
	protected void onEnd() {
		super.onEnd();
		if ( zipFile != null ) {
			try {
				zipFile.close();
			} catch (IOException e) {
				log().warn("Error closing zip file", e);
			}
		}
	}

	public static class BackupFileExtractor {
	
		private ZipFile zipFile;

		public BackupFileExtractor(ZipFile zipFile) {
			this.zipFile = zipFile;
		}
		
		public File extractInfoFile() {
			return extract(SurveyBackupJob.INFO_FILE_NAME);
		}
		
		public File extractIdmlFile() {
			return extract(SurveyBackupJob.SURVEY_XML_ENTRY_NAME);
		}
		
		public File extract(String entryName) {
			return extract(entryName, true);
		}
		
		public File extract(String entryName, boolean required) {
			ZipEntry entry = findEntry(entryName);
			if ( entry == null ) {
				if ( required ) {
					throw new RuntimeException("Entry not found in packaged file: " + entryName);
				} else {
					return null;
				}
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
			if ( ! path.endsWith(SurveyBackupJob.ZIP_FOLDER_SEPARATOR) ) {
				path += SurveyBackupJob.ZIP_FOLDER_SEPARATOR;
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

		public boolean containsEntry(String name) {
			ZipEntry entry = findEntry(name);
			return entry != null;
		}
		
		public boolean containsEntriesInPath(String path) {
			List<String> entryNames = listEntriesInPath(path);
			return ! entryNames.isEmpty();
		}
		
	}

	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
	
	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
	public SamplingDesignManager getSamplingDesignManager() {
		return samplingDesignManager;
	}
	
	public void setSamplingDesignManager(SamplingDesignManager samplingDesignManager) {
		this.samplingDesignManager = samplingDesignManager;
	}
	
	public SpeciesManager getSpeciesManager() {
		return speciesManager;
	}
	
	public void setSpeciesManager(SpeciesManager speciesManager) {
		this.speciesManager = speciesManager;
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}
	
	public String getSurveyUri() {
		return surveyUri;
	}
	
	public void setSurveyUri(String surveyUri) {
		this.surveyUri = surveyUri;
	}

	public boolean isRestoreIntoPublishedSurvey() {
		return restoreIntoPublishedSurvey;
	}
	
	public void setRestoreIntoPublishedSurvey(boolean restoreIntoPublishedSurvey) {
		this.restoreIntoPublishedSurvey = restoreIntoPublishedSurvey;
	}
	
	public boolean isValidateSurvey() {
		return validateSurvey;
	}
	
	public void setValidateSurvey(boolean validateSurvey) {
		this.validateSurvey = validateSurvey;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public SurveyBackupInfo getBackupInfo() {
		return backupInfo;
	}

}
