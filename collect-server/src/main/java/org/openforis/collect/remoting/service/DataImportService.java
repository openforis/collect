package org.openforis.collect.remoting.service;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	
	private static final Log log = LogFactory.getLog(DataImportService.class);	
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private JobManager jobManager;
	
	private File packagedFile;
	
	private DataRestoreSummaryJob summaryJob;
	private DataRestoreJob dataRestoreJob;
	
	@Secured("ROLE_ADMIN")
	public JobProxy startSummaryCreation(String filePath, String selectedSurveyUri, boolean overwriteAll) throws DataImportExeption {
		if ( summaryJob == null || ! summaryJob.isRunning() ) {
			log.info("Starting data import summary creation");
			
			packagedFile = new File(filePath);

			log.info("Using file: " + packagedFile.getAbsolutePath());

			DataRestoreSummaryJob job = jobManager.createJob(DataRestoreSummaryJob.class);
			job.setFile(packagedFile);
			job.setSurveyUri(selectedSurveyUri);

			resetJobs();
			this.summaryJob = job;
			
			log.info("Starting summary creation job");

			jobManager.start(job);
			
			log.info("Summary creation Job started");
		} else {
			log.info("Summary creation job already running");
		}
		return getCurrentJob();
	}
	
	@Secured("ROLE_ADMIN")
	public JobProxy startImport(List<Integer> entryIdsToImport) throws Exception {
		if ( dataRestoreJob == null || ! dataRestoreJob.isRunning() ) {
			log.info("Starting data restore");

			DataRestoreJob job = jobManager.createJob(DataRestoreJob.class);
			job.setFile(packagedFile);
			job.setPackagedSurvey(summaryJob.getPackagedSurvey());
			job.setPublishedSurvey(summaryJob.getPublishedSurvey());
			job.setEntryIdsToImport(entryIdsToImport);
			job.setRestoreUploadedFiles(true);
			
			resetJobs();
			this.dataRestoreJob = job;
			
			log.info("Starting data restore job");
			jobManager.start(job);
			log.info("Data restore job started");
		} else {
			log.info("Data restore job already running");
		}
		return getCurrentJob();
	}

	@Secured("ROLE_ADMIN")
	public JobProxy getCurrentJob() {
		JobProxy proxy = null;
		if ( summaryJob != null ) {
			log.info("Active job: summary");
			proxy = new DataRestoreSummaryJobProxy(summaryJob);
		} else if ( dataRestoreJob != null) {
			log.info("Active job: data restore");
			proxy = new DataRestoreJobProxy(dataRestoreJob);
		}
		log.info("Job status: " + (proxy == null ? " inactive": proxy.getStatus()));
		return proxy;
	}
	
	@Secured("ROLE_ADMIN")
	public DataImportSummaryProxy getSummary() {
		if ( summaryJob != null ) {
			DataImportSummary summary = summaryJob.getSummary();
			SessionState sessionState = sessionManager.getSessionState();
			Locale locale = sessionState.getLocale();
			DataImportSummaryProxy proxy = new DataImportSummaryProxy(summary, locale);
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

	private void resetJobs() {
		this.summaryJob = null;
		this.dataRestoreJob = null;
	}
	
}
