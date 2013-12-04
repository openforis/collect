package org.openforis.collect.remoting.service;

import java.io.File;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.dataimport.CSVDataImportProcess;
import org.openforis.collect.manager.exception.DataImportExeption;
import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.collect.manager.referencedataimport.proxy.ReferenceDataImportStatusProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class CSVDataImportService extends ReferenceDataImportService<ReferenceDataImportStatusProxy, CSVDataImportProcess> { 
	
	private static final String IMPORT_FILE_NAME = "data_import.csv";
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private ApplicationContext applicationContext;
	
	public CSVDataImportService() {
		super(IMPORT_FILE_NAME);
	}

	@Secured("ROLE_ADMIN")
	public ReferenceDataImportStatusProxy start(int parentEntityId, CollectRecord.Step step) throws DataImportExeption {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			File importFile = getImportFile();
			SessionState sessionState = sessionManager.getSessionState();
			CollectSurvey survey = sessionState.getActiveSurvey();
			importProcess = applicationContext.getBean(CSVDataImportProcess.class);
			importProcess.setFile(importFile);
			importProcess.setSurvey(survey);
			importProcess.setParentEntityDefinitionId(parentEntityId);
			importProcess.setStep(step);
			importProcess.init();
			ProcessStatus status = importProcess.getStatus();
			if ( status != null && ! importProcess.getStatus().isError() ) {
				startProcessThread();
			}
		}
		return getStatus();
	}

	@Override
	@Secured("ROLE_ADMIN")
	public ReferenceDataImportStatusProxy getStatus() {
		if ( importProcess == null ) {
			return null;
		} else {
			return new ReferenceDataImportStatusProxy(importProcess.getStatus());
		}
	}
	
}
