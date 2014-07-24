package org.openforis.collect.remoting.service;

import java.io.File;

import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignImportProcess;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignImportStatus;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.remoting.service.samplingdesignimport.proxy.SamplingDesignImportStatusProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignImportService extends ReferenceDataImportService<SamplingDesignImportStatusProxy, SamplingDesignImportProcess> {
	
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SessionManager sessionManager;
	
	@Secured("ROLE_ADMIN")
	public SamplingDesignImportStatusProxy start(String tempFileName, int surveyId, boolean work, boolean overwriteAll) throws DataImportExeption, SurveyImportException {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			File importFile = new File(tempFileName);
			CollectSurvey survey = work ? surveyManager.loadSurveyWork(surveyId): surveyManager.getById(surveyId);
			if ( survey.getSamplingDesignCodeList() == null ) {
				surveyManager.addSamplingDesignCodeList(survey);
			}
			importProcess = new SamplingDesignImportProcess(samplingDesignManager, surveyManager, 
					survey, importFile, overwriteAll);
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
			if ( status.isComplete() ) {
				updateSessionSurvey();
			}
			return new SamplingDesignImportStatusProxy(status);
		} else {
			return null;
		}
	}

	private void updateSessionSurvey() {
		CollectSurvey survey = importProcess.getSurvey();
		if ( survey.isWork() ) {
			sessionManager.getActiveDesignerSurvey().setSamplingPoints(survey.getSamplingPoints());
		}
	}
	
}
