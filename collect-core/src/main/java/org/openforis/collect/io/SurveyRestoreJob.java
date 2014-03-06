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
import org.openforis.collect.io.metadata.IdmlImportTask;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignImportTask;
import org.openforis.collect.io.metadata.species.SpeciesBackupImportTask;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.WorkerStatusChangeEvent;
import org.openforis.concurrency.WorkerStatusChangeListener;
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

	//parameters
	private transient File file;
	private String publishedSurveyUri;
	private boolean updatingExistingSurvey;
	private boolean updatingPublishedSurvey;
	private String surveyName;

	//temporary instance variables
	private transient ZipFile zipFile;
	
	private transient CollectSurvey survey;
	
	@Override
	public void init() {
		try {
			zipFile = new ZipFile(file);
			addIdmlImportTask();
			addSamplingDesignImportTask();
			addSpeciesImportTask();
			if ( updatingPublishedSurvey && isDataIncluded() ) {
				addDataImportTask();
			}
			super.init();
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}

	private void addIdmlImportTask() throws IOException {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
		File idmlFile = backupFileExtractor.extractIdmlFile();
		final IdmlImportTask task = createTask(IdmlImportTask.class);
		task.setFile(idmlFile);
		task.setPublishedSurveyUri(publishedSurveyUri);
		task.setName(surveyName);
		task.setUpdatingExistingSurvey(updatingExistingSurvey);
		task.setUpdatingPublishedSurvey(updatingPublishedSurvey);
		task.addStatusChangeListener(new WorkerStatusChangeListener() {
			@Override
			public void statusChanged(WorkerStatusChangeEvent event) {
				if ( event.getTo() == Status.COMPLETED ) {
					//get output survey and set it into job instance instance variable
					SurveyRestoreJob.this.survey = task.getSurvey();
				}
			}
		});
		addTask(task);
	}
	
	private void addSamplingDesignImportTask() throws IOException {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
		File samplingDesignFile = backupFileExtractor.extract(SurveyBackupJob.SAMPLING_DESIGN_ENTRY_NAME);
		final SamplingDesignImportTask task = createTask(SamplingDesignImportTask.class);
		task.setFile(samplingDesignFile);
		task.setOverwriteAll(true);
		task.addStatusChangeListener(new WorkerStatusChangeListener() {
			@Override
			public void statusChanged(WorkerStatusChangeEvent event) {
				if ( event.getTo() == Status.RUNNING ) {
					//set "survey" instance variable
					task.setSurvey(survey);
				}
			}
		});
		addTask(task);
	}

	private void addSpeciesImportTask() throws IOException {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
		List<String> speciesFilesNames = backupFileExtractor.listEntriesInPath(SurveyBackupJob.SPECIES_FOLDER);
		for (String speciesFileName : speciesFilesNames) {
			final SpeciesBackupImportTask task = createTask(SpeciesBackupImportTask.class);
			File file = backupFileExtractor.extract(speciesFileName);
			String taxonomyName = FilenameUtils.getBaseName(speciesFileName);
			task.setFile(file);
			task.setTaxonomyName(taxonomyName);
			task.setOverwriteAll(true);
			task.addStatusChangeListener(new WorkerStatusChangeListener() {
				@Override
				public void statusChanged(WorkerStatusChangeEvent event) {
					if ( event.getTo() == Status.RUNNING ) {
						//set "survey" instance variable
						task.setSurvey(survey);
					}
				}
			});
			addTask(task);
		}
	}

	private boolean isDataIncluded() throws IOException {
		BackupFileExtractor backupFileExtractor = new BackupFileExtractor(zipFile);
		List<String> dataEntries = backupFileExtractor.listEntriesInPath(SurveyBackupJob.DATA_FOLDER);
		return ! dataEntries.isEmpty();
	}

	private void addDataImportTask() {
		
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
		
		public File extractIdmlFile() throws IOException {
			return extract(SurveyBackupJob.SURVEY_XML_ENTRY_NAME);
		}
		
		public File extract(String entryName) throws IOException {
			ZipEntry entry = findEntry(entryName);
			if ( entry == null ) {
				return null;
			} else {
				return extract(entry);
			}
		}

		private File extract(ZipEntry entry) throws IOException {
			String entryName = entry.getName();
			String fileName = FilenameUtils.getName(entryName);
			File tempFile = File.createTempFile("collect", fileName);
			InputStream is = zipFile.getInputStream(entry);
			FileUtils.copyInputStreamToFile(is, tempFile);
			return tempFile;
		}
		
		public List<String> listEntriesInPath(String folder) throws IOException {
			List<String> result = new ArrayList<String>();
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while ( zipEntries.hasMoreElements() ) {
				ZipEntry zipEntry = zipEntries.nextElement();
				String name = zipEntry.getName();
				int lastIndexOfSlash = name.lastIndexOf('/');
				if ( lastIndexOfSlash > 0 ) {
					String entryPath = name.substring(0, lastIndexOfSlash);
					if ( entryPath.equals(folder) ) {
						result.add(name);
					}
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
		
		public ZipEntry findEntry(String entryName) throws IOException {
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
