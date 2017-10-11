package org.openforis.collect.remoting.service;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.io.data.CSVDataImportJob;
import org.openforis.collect.io.data.CSVDataImportJob.CSVDataImportInput;
import org.openforis.collect.io.data.TransactionalCSVDataImportJob;
import org.openforis.collect.io.data.csv.CSVDataImportSettings;
import org.openforis.collect.io.data.proxy.DataImportStatusProxy;
import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class CSVDataImportService { 
	
	@Autowired
	private RecordSessionManager sessionManager;
	@Autowired
	private CollectJobManager jobManager;
	private CSVDataImportJob importJob;
	
	protected void init() {
	}
	
	@Secured("ROLE_ADMIN")
	public DataImportStatusProxy start(String tempFileName, int parentEntityId, Step step, 
			boolean transactional, boolean validateRecords, 
			boolean insertNewRecords, String newRecordVersionName,
			boolean deleteExistingEntities) throws DataImportExeption {
		if ( importJob == null || ! importJob.isRunning() ) {
			File importFile = new File(tempFileName);
			SessionState sessionState = sessionManager.getSessionState();
			CollectSurvey survey = sessionState.getActiveSurvey();
			if (transactional) {
				importJob = jobManager.createJob(TransactionalCSVDataImportJob.class);
			} else {
				importJob = jobManager.createJob(CSVDataImportJob.BEAN_NAME, CSVDataImportJob.class);
			}
			CSVDataImportSettings settings = new CSVDataImportSettings();
			settings.setRecordValidationEnabled(validateRecords);
			settings.setInsertNewRecords(insertNewRecords);
			settings.setNewRecordVersionName(newRecordVersionName);
			settings.setDeleteExistingEntities(deleteExistingEntities);
			
			Set<Step> steps = step == null ? new HashSet<Step>(Arrays.asList(Step.values())): Collections.singleton(step);
			CSVDataImportInput input = new CSVDataImportInput(importFile, survey, steps, parentEntityId, settings);
			importJob.setInput(input);
			jobManager.start(importJob);
		}
		return getStatus();
	}

	@Secured("ROLE_ADMIN")
	public DataImportStatusProxy getStatus() {
		return importJob == null ? null : new DataImportStatusProxy(importJob);
	}
	
	@Secured("ROLE_ADMIN")
	public void cancel() {
		if ( importJob != null ) {
			importJob.abort();
		}
	}
}
