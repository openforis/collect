package org.openforis.collect.remoting.service;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.openforis.collect.Proxy;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.CSVDataExportProcess;
import org.openforis.collect.io.data.csv.CSVExportConfiguration;
import org.openforis.collect.io.data.proxy.DataExportProcessProxy;
import org.openforis.collect.io.proxy.SurveyBackupJobProxy;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
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
	
	private CSVDataExportProcess dataExportProcess;
	private SurveyBackupJob backupJob;
	
	@Transactional
	public Proxy export(String rootEntityName, int stepNumber, Integer entityId, boolean includeAllAncestorAttributes, 
			boolean includeEnumeratedEntities, boolean includeCompositeAttributeMergedColumn, 
			boolean codeAttributeExpanded, boolean onlyOwnedRecords, String[] rootEntityKeyValues) throws IOException {
		if ( dataExportProcess == null || ! dataExportProcess.getStatus().isRunning() ) {
			resetJobs();
			
			SessionState sessionState = sessionManager.getSessionState();
			CollectSurvey survey = sessionState.getActiveSurvey();
			
			File outputFile = File.createTempFile("collect_data_export_" + survey.getName(), ".zip");
			
			Step step = Step.valueOf(stepNumber);

			//prepare record filter
			Schema schema = survey.getSchema();
			EntityDefinition rootEntityDefn = schema.getRootEntityDefinition(rootEntityName);
			
			RecordFilter recordFilter = createRecordFilter(rootEntityDefn.getId(), onlyOwnedRecords, rootEntityKeyValues);
			
			//filter by record step
			recordFilter.setStepGreaterOrEqual(step);
			
			//instantiate process
			CSVDataExportProcess process = appContext.getBean(CSVDataExportProcess.class);
			process.setOutputFile(outputFile);
			process.setRecordFilter(recordFilter);
			process.setEntityId(entityId);
			process.setAlwaysGenerateZipFile(true);
			CSVExportConfiguration config = new CSVExportConfiguration();
			config.setIncludeAllAncestorAttributes(includeAllAncestorAttributes);
			config.setIncludeEnumeratedEntities(includeEnumeratedEntities);
			config.setIncludeCompositeAttributeMergedColumn(includeCompositeAttributeMergedColumn);
			config.setCodeAttributeExpanded(codeAttributeExpanded);
			process.setConfig(config);
			
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
			CollectSurvey survey = sessionState.getActiveSurvey();
			
			RecordFilter filter = createRecordFilter(null, onlyOwnedRecords, rootEntityKeyValues);
			
			SurveyBackupJob job = jobManager.createJob(SurveyBackupJob.class);
			job.setSurvey(survey);
			job.setIncludeData(true);
			job.setIncludeRecordFiles(includeRecordFiles);
			job.setRecordFilter(filter);
			
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
			return new DataExportProcessProxy(dataExportProcess);
		} else {
			return null;
		}
	}
	
}
