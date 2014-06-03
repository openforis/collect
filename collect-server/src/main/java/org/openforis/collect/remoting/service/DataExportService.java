package org.openforis.collect.remoting.service;

import java.io.File;

import javax.servlet.ServletContext;

import org.openforis.collect.Proxy;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.CSVDataExportProcess;
import org.openforis.collect.io.data.DataExportStatus;
import org.openforis.collect.io.proxy.SurveyBackupJobProxy;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.proxy.DataExportStatusProxy;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.User;
import org.openforis.collect.utils.ExecutorServiceUtil;
import org.openforis.collect.web.session.SessionState;
import org.openforis.concurrency.JobManager;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportService {

	//private static Log LOG = LogFactory.getLog(DataExportService.class);

	private static final String EXPORT_PATH = "export";
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired 
	private ServletContext servletContext;
	@Autowired
	private ApplicationContext appContext;
	@Autowired
	private JobManager jobManager;
	
	private File exportDirectory;
	
	private AbstractProcess<Void, DataExportStatus> dataExportProcess;

	private SurveyBackupJob backupJob;
	
	public void init() {
		String exportRealPath = servletContext.getRealPath(EXPORT_PATH);
		exportDirectory = new File(exportRealPath);
		if ( exportDirectory.exists() ) {
			exportDirectory.delete();
		}
		if ( ! exportDirectory.mkdirs() && ! exportDirectory.canRead() ) {
			throw new IllegalStateException("Cannot access export directory: " + exportRealPath);
		}
	}

	@Transactional
	public Proxy export(String rootEntityName, int stepNumber, Integer entityId, boolean includeAllAncestorAttributes, boolean onlyOwnedRecords, String[] rootEntityKeyValues) {
		if ( dataExportProcess == null || ! dataExportProcess.getStatus().isRunning() ) {
			resetJobs();
			
			SessionState sessionState = sessionManager.getSessionState();
			
			File exportDir = new File(exportDirectory, sessionState.getSessionId());
			if ( ! exportDir.exists() && ! exportDir.mkdirs() ) {
				throw new IllegalStateException("Cannot create export directory: " + exportDir.getAbsolutePath());
			}
			CollectSurvey activeSurvey = sessionState.getActiveSurvey();
			Step step = Step.valueOf(stepNumber);
			File outputFile = new File(exportDir, "data.zip");

			//prepare record filter
			Schema schema = activeSurvey.getSchema();
			EntityDefinition rootEntityDefn = schema.getRootEntityDefinition(rootEntityName);
			
			RecordFilter recordFilter = createRecordFilter(rootEntityDefn.getId(), onlyOwnedRecords, rootEntityKeyValues);
			
			//filter by record step
			recordFilter.setStepGreaterOrEqual(step);
			
			//instantiate process
			CSVDataExportProcess process = appContext.getBean(CSVDataExportProcess.class);
			process.setOutputFile(outputFile);
			process.setRecordFilter(recordFilter);
			process.setEntityId(entityId);
			process.setIncludeAllAncestorAttributes(includeAllAncestorAttributes);
			process.setAlwaysGenerateZipFile(true);
			
			process.init();
			
			//start process
			dataExportProcess = process;
			ExecutorServiceUtil.executeInCachedPool(process);
		}
		return getCurrentJob();
	}
	
	@Transactional
	public Proxy fullExport(boolean includeRecordFiles, boolean onlyOwnedRecords, String[] rootEntityKeyValues) {
		if ( backupJob == null || ! backupJob.isRunning() ) {
			resetJobs();
			
			SessionState sessionState = sessionManager.getSessionState();
			File exportDir = new File(exportDirectory, sessionState.getSessionId());
			if ( ! exportDir.exists() && ! exportDir.mkdirs() ) {
				throw new IllegalStateException("Cannot create export directory: " + exportDir.getAbsolutePath());
			}
			CollectSurvey survey = sessionState.getActiveSurvey();
			File outputFile = new File(exportDir, "data.zip");

			RecordFilter filter = createRecordFilter(null, onlyOwnedRecords, rootEntityKeyValues);
			
			SurveyBackupJob job = jobManager.createJob(SurveyBackupJob.class);
			job.setSurvey(survey);
			job.setIncludeData(true);
			job.setIncludeRecordFiles(includeRecordFiles);
			job.setRecordFilter(filter);
			job.setOutputFile(outputFile);
			
			backupJob = job;
			
			jobManager.start(job);
		}
		return getCurrentJob();
	}

	private RecordFilter createRecordFilter(Integer rootEntityId, boolean onlyOwnedRecords, String[] rootEntityKeyValues) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		
		RecordFilter recordFilter = new RecordFilter(activeSurvey, rootEntityId);
		
		//filter by record owner
		if ( onlyOwnedRecords ) {
			User user = sessionState.getUser();
			recordFilter.setOwnerId(user.getId());
		}
		
		//filter by root entity keys
		recordFilter.setKeyValues(rootEntityKeyValues);
		
		return recordFilter;
	}

	private void resetJobs() {
		backupJob = null;
		dataExportProcess = null;
	}

	public void abort() {
		if ( dataExportProcess != null ) {
			dataExportProcess.cancel();
		}
		if ( backupJob != null ) {
			backupJob.abort();
		}
	}

	public Proxy getCurrentJob() {
		if ( backupJob != null ) {
			return new SurveyBackupJobProxy(backupJob);
		} else if ( dataExportProcess != null ) {
			return new DataExportStatusProxy(dataExportProcess.getStatus());
		} else {
			return null;
		}
	}
	
}
