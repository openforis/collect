/**
 * 
 */
package org.openforis.collect.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.metadata.SurveyBackupJob;
import org.openforis.collect.io.samplingdesign.SamplingDesignImportTask;
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
			super.init();
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}

	private void addSamplingDesignImportTask() throws IOException {
		File samplingDesignFile = ZipFileExtractor.extract(zipFile, SurveyBackupJob.SAMPLING_DESIGN_ENTRY_NAME);
		final SamplingDesignImportTask task = createTask(SamplingDesignImportTask.class);
		task.setFile(samplingDesignFile);
		task.setOverwriteAll(true);
		task.addStatusChangeListener(new WorkerStatusChangeListener() {
			@Override
			public void statusChanged(WorkerStatusChangeEvent event) {
				if ( event.getTo() == Status.RUNNING ) {
					task.setSurvey(survey);
				}
			}
		});
		addTask(task);
	}

	private void addIdmlImportTask() throws IOException {
		File idmlFile = ZipFileExtractor.extractIdmlFile(zipFile);
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
					SurveyRestoreJob.this.survey = task.getSurvey();
				}
			}
		});
		addTask(task);
	}
	
	@Override
	protected void execute() throws Throwable {
		super.execute();
		IOUtils.closeQuietly(zipFile);
	}
	
	public static class ZipFileExtractor {
		
		public static File extractIdmlFile(ZipFile zipFile) throws IOException {
			return extract(zipFile, SurveyBackupJob.SURVEY_XML_ENTRY_NAME);
		}
		
		public static File extract(ZipFile zipFile, String entryName) throws IOException {
			InputStream is = find(zipFile, entryName);
			if ( is == null ) {
				return null;
			} else {
				String name = FilenameUtils.getName(entryName);
				File tempFile = File.createTempFile("collect", name);
				FileUtils.copyInputStreamToFile(is, tempFile);
				return tempFile;
			}
		}
		
		public static InputStream find(ZipFile zipFile, String entryName) throws IOException {
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while ( zipEntries.hasMoreElements() ) {
				ZipEntry zipEntry = zipEntries.nextElement();
				String name = zipEntry.getName();
				if ( ! zipEntry.isDirectory() && name.equals(entryName)  ) {
					InputStream is = zipFile.getInputStream(zipEntry);
					return is;
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
