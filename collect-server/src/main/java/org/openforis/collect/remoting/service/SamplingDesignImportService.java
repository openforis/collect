package org.openforis.collect.remoting.service;

import java.io.File;

import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignImportProcess;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignImportStatus;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.remoting.service.samplingdesignimport.proxy.SamplingDesignImportStatusProxy;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
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
	private RecordSessionManager sessionManager;
	
	@Secured("ROLE_ADMIN")
	public SamplingDesignImportStatusProxy start(String tempFileName, int surveyId, boolean temporary, boolean overwriteAll) throws DataImportExeption, SurveyImportException {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			File importFile = new File(tempFileName);
			CollectSurvey survey = temporary ? surveyManager.loadSurvey(surveyId): surveyManager.getById(surveyId);
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
		CollectSurvey processSurvey = importProcess.getSurvey();
		if ( processSurvey.isTemporary() ) {
			ReferenceDataSchema processReferenceDataSchema = processSurvey.getReferenceDataSchema();
			SamplingPointDefinition processSamplingPoint = processReferenceDataSchema == null ? null: processReferenceDataSchema.getSamplingPointDefinition();

			CollectSurvey editedSurvey = sessionManager.getActiveDesignerSurvey();
			ReferenceDataSchema referenceDataSchema = editedSurvey.getReferenceDataSchema();
			if ( referenceDataSchema == null ) {
				referenceDataSchema = new ReferenceDataSchema();
				editedSurvey.setReferenceDataSchema(referenceDataSchema);
			}
			referenceDataSchema.setSamplingPointDefinition(processSamplingPoint);
		}
	}
	
}
