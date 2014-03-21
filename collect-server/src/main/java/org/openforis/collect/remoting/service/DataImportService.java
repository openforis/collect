package org.openforis.collect.remoting.service;

import java.io.File;
import java.util.List;

import javax.servlet.ServletContext;

import org.openforis.collect.io.data.DataImportSummary;
import org.openforis.collect.io.data.DataRestoreJob;
import org.openforis.collect.io.data.DataRestoreSummaryJob;
import org.openforis.collect.io.data.proxy.DataRestoreJobProxy;
import org.openforis.collect.io.data.proxy.DataRestoreSummaryJobProxy;
import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.remoting.service.dataimport.DataImportSummaryProxy;
import org.openforis.collect.web.session.SessionState;
import org.openforis.concurrency.JobManager;
import org.openforis.concurrency.proxy.JobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportService {
	
	private static final String IMPORT_PATH = "import";
	
	private static final String FILE_NAME = "data_import.zip";
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired 
	private ServletContext servletContext;
	@Autowired
	private JobManager jobManager;
	
	private File packagedFile;
	private File importDirectory;
	
	private DataRestoreSummaryJob summaryJob;
	private DataRestoreJob dataRestoreJob;
	
	protected void init() {
		String importRealPath = servletContext.getRealPath(IMPORT_PATH);
		importDirectory = new File(importRealPath);
		if ( importDirectory.exists() ) {
			importDirectory.delete();
		}
		if ( ! importDirectory.mkdirs() && ! importDirectory.canRead() ) {
			throw new IllegalStateException("Cannot access import directory: " + importRealPath);
		}
	}
	
	@Secured("ROLE_ADMIN")
	public JobProxy startSummaryCreation(String selectedSurveyUri, boolean overwriteAll) throws DataImportExeption {
		if ( summaryJob == null || ! summaryJob.isRunning() ) {
			SessionState sessionState = sessionManager.getSessionState();
			File userImportFolder = new File(importDirectory, sessionState.getSessionId());
			packagedFile = new File(userImportFolder, FILE_NAME);
			
			summaryJob = jobManager.createJob(DataRestoreSummaryJob.class);
			summaryJob.setFile(packagedFile);
			summaryJob.setSurveyUri(selectedSurveyUri);
			
			jobManager.start(summaryJob);
		}
		return getCurrentJob();
	}
	
	@Secured("ROLE_ADMIN")
	public JobProxy startImport(List<Integer> entryIdsToImport) throws Exception {
		DataRestoreJob job = jobManager.createJob(DataRestoreJob.class);
		job.setFile(packagedFile);
		job.setPackagedSurvey(summaryJob.getPackagedSurvey());
		job.setPublishedSurvey(summaryJob.getPublishedSurvey());
		job.setEntryIdsToImport(entryIdsToImport);
		job.setRestoreUploadedFiles(true);

		jobManager.start(job);

		this.summaryJob = null;
		this.dataRestoreJob = job;
		
		return getCurrentJob();
	}
	
	@Secured("ROLE_ADMIN")
	public JobProxy getCurrentJob() {
		JobProxy proxy = null;
		if ( summaryJob != null ) {
			proxy = new DataRestoreSummaryJobProxy(summaryJob);
		} else if ( dataRestoreJob != null) {
			proxy = new DataRestoreJobProxy(dataRestoreJob);
		}
		return proxy;
	}
	
	@Secured("ROLE_ADMIN")
	public DataImportSummaryProxy getSummary() {
		if ( summaryJob != null ) {
			DataImportSummary summary = summaryJob.getSummary();
			DataImportSummaryProxy proxy = new DataImportSummaryProxy(summary);
			return proxy;
		} else {
			return null;
		}
	}

	@Secured("ROLE_ADMIN")
	public void cancel() {
		if ( summaryJob != null ) {
			summaryJob.abort();
		} else if ( dataRestoreJob != null ) {
			dataRestoreJob.abort();
		}
	}
	
}
