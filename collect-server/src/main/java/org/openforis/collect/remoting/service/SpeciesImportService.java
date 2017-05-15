package org.openforis.collect.remoting.service;

import java.io.File;

import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.speciesimport.SpeciesImportProcess;
import org.openforis.collect.manager.speciesimport.SpeciesImportStatus;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.remoting.service.speciesimport.proxy.SpeciesImportStatusProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesImportService extends ReferenceDataImportService<SpeciesImportStatusProxy, SpeciesImportProcess> {
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	@Qualifier("sessionManager")
	private SessionManager sessionManager;
	
	@Secured("ROLE_ADMIN")
	public SpeciesImportStatusProxy start(String tempFileName, CSVFileOptions csvFileOptions, 
			int surveyId, int taxonomyId, boolean overwriteAll) throws DataImportExeption {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			CollectSurvey survey = sessionManager.getActiveDesignerSurvey();
			if (survey.getId() != surveyId) {
				throw new IllegalStateException("Error importing species list: different survey found in session");
			}
			File importFile = new File(tempFileName);
			importProcess = new SpeciesImportProcess(surveyManager, speciesManager, survey, taxonomyId, 
					importFile, csvFileOptions, overwriteAll);
			importProcess.init();
			SpeciesImportStatus status = importProcess.getStatus();
			if ( status != null && ! importProcess.getStatus().isError() ) {
				startProcessThread();
			}
		}
		return getStatus();
	}

	@Secured("ROLE_ADMIN")
	public SpeciesImportStatusProxy getStatus() {
		if ( importProcess != null ) {
			SpeciesImportStatus status = importProcess.getStatus();
			return new SpeciesImportStatusProxy(status);
		} else {
			return null;
		}
	}
	
}
