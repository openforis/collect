package org.openforis.collect.remoting.service;

import java.io.File;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.samplingDesignImport.SamplingDesignImportProcess;
import org.openforis.collect.manager.samplingDesignImport.SamplingDesignImportStatus;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.remoting.service.dataImport.DataImportExeption;
import org.openforis.collect.remoting.service.samplingDesignImport.proxy.SamplingDesignImportStatusProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignImportService extends ReferenceDataImportService<SamplingDesignImportStatusProxy, SamplingDesignImportProcess> {
	
	private static final String IMPORT_FILE_NAME = "sampling_design.csv";
	
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SurveyManager surveyManager;
	
	public SamplingDesignImportService() {
		super(IMPORT_FILE_NAME);
	}
	
	@Secured("ROLE_ADMIN")
	public SamplingDesignImportStatusProxy start(int surveyId, boolean work, boolean overwriteAll) throws DataImportExeption {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			File importFile = getImportFile();
			CollectSurvey survey = work ? surveyManager.loadSurveyWork(surveyId): surveyManager.getById(surveyId);
			importProcess = new SamplingDesignImportProcess(samplingDesignManager, survey, work, importFile, overwriteAll);
			importProcess.init();
			SamplingDesignImportStatus status = importProcess.getStatus();
			if ( status != null && ! importProcess.getStatus().isError() ) {
				startProcessThread();
			}
		}
		return getStatus();
	}

	@Secured("ROLE_ADMIN")
	public SamplingDesignImportStatusProxy getStatus() {
		if ( importProcess != null ) {
			SamplingDesignImportStatus status = importProcess.getStatus();
			return new SamplingDesignImportStatusProxy(status);
		} else {
			return null;
		}
	}
	
	
}
