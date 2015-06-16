package org.openforis.collect.remoting.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openforis.collect.Proxy;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyBackupJob.OutputFormat;
import org.openforis.collect.io.data.CSVDataExportProcess;
import org.openforis.collect.io.data.backup.BackupStorageManager;
import org.openforis.collect.io.data.csv.CSVExportConfiguration;
import org.openforis.collect.io.data.proxy.DataExportProcessProxy;
import org.openforis.collect.io.proxy.SurveyBackupJobProxy;
import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.User;
import org.openforis.collect.utils.ExecutorServiceUtil;
import org.openforis.collect.web.session.SessionState;
import org.openforis.concurrency.JobManager;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportService {

	private static Log LOG = LogFactory.getLog(DataExportService.class);

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
	@Autowired
	private BackupStorageManager backupStorageManager;
	@Autowired
	private ConfigurationManager configurationManager;
	
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
			
			RecordFilter recordFilter = createRecordFilter(survey, rootEntityDefn.getId(), onlyOwnedRecords, rootEntityKeyValues);
			
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
			process.setConfiguration(config);
			
			process.init();
			
			//start process
			dataExportProcess = process;
			ExecutorServiceUtil.executeInCachedPool(process);
		}
		return getCurrentJob();
	}
	
	@Transactional
	public Proxy backup(String surveyName) {
		CollectSurvey survey = surveyManager.get(surveyName);
		return fullExport(survey, true, false, null, true);
	}

	@Transactional
	public Proxy fullExport(boolean includeRecordFiles, boolean onlyOwnedRecords, String[] rootEntityKeyValues) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey survey = sessionState.getActiveSurvey();
		return fullExport(survey, includeRecordFiles, onlyOwnedRecords, rootEntityKeyValues, false);
	}
	
	@Transactional
	public Proxy fullExport(CollectSurvey survey, boolean includeRecordFiles, boolean onlyOwnedRecords, String[] rootEntityKeyValues, boolean full) {
		if ( backupJob == null || ! backupJob.isRunning() ) {
			resetJobs();
			
			RecordFilter filter = createRecordFilter(survey, null, onlyOwnedRecords, rootEntityKeyValues);
			
			SurveyBackupJob job = jobManager.createJob(SurveyBackupJob.class);
			job.setFull(full);
			if (full) {
				job.setOutputFormat(OutputFormat.DESKTOP_FULL);
			} else {
				job.setOutputFormat(OutputFormat.ONLY_DATA);
			}
			job.setSurvey(survey);
			job.setIncludeData(true);
			job.setIncludeRecordFiles(includeRecordFiles);
			job.setRecordFilter(filter);
			backupJob = job;
			
			jobManager.start(job);
		}
		return getCurrentJob();
	}
	
	public Map<String, Object> getLastBackupInfo(String surveyName) {
		final Date date = backupStorageManager.getLastBackupDate(surveyName);
		CollectSurvey survey = surveyManager.get(surveyName);
		RecordFilter filter = new RecordFilter(survey);
		filter.setModifiedSince(date);
		final int updatedRecordsSinceBackupDateCount = recordManager.countRecords(filter);
		@SuppressWarnings("serial")
		Map<String, Object> map = new HashMap<String, Object>() {{
			put("date", date);
			put("updatedRecordsSinceBackup", updatedRecordsSinceBackupDateCount);
		}};
		return map;
	}
	
	@Secured("ROLE_ADMIN")
	public String sendBackupToRemoteClone(String surveyName) {
		try {
			String remoteCollectCloneUrl = configurationManager.getConfiguration().get(ConfigurationItem.REMOTE_CLONE_URL);
			File lastBackupFile = backupStorageManager.getLastBackupFile(surveyName);
			
			HttpClientBuilder clientBuilder = HttpClientBuilder.create();
			String postUrl = remoteCollectCloneUrl + "/survey-data/restore-remotely.json";
			HttpPost httppost = new HttpPost(postUrl);
			// Request parameters and other properties.
			MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
			multipartEntityBuilder.addBinaryBody("file", lastBackupFile);
		    httppost.setEntity(multipartEntityBuilder.build());
		    
			//Execute and get the response.
			CloseableHttpClient httpClient = clientBuilder.build();
			HttpResponse response = httpClient.execute(httppost);
			HttpEntity entity = response.getEntity();
	
			if (entity != null) {
			    InputStream is = entity.getContent();
			    try {
			    	StringWriter writer = new StringWriter();
			    	IOUtils.copy(is, writer, "UTF-8");
			    	String result = writer.toString();
			    	System.out.println(result);
			    } finally {
			        is.close();
			    }
			}
		} catch (Exception e) {
			LOG.error(e);
		}
		return null;
	}

	private RecordFilter createRecordFilter(CollectSurvey survey, Integer rootEntityId, boolean onlyOwnedRecords, String[] rootEntityKeyValues) {
		RecordFilter recordFilter = new RecordFilter(survey, rootEntityId);
		
		//filter by record owner
		if ( onlyOwnedRecords ) {
			SessionState sessionState = sessionManager.getSessionState();
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
