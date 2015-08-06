package org.openforis.collect.remoting.service;

import java.io.File;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.codelistimport.CodeListImportProcess;
import org.openforis.collect.manager.codelistimport.CodeListImportStatus;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.remoting.service.codelistimport.proxy.CodeListImportStatusProxy;
import org.openforis.idm.metamodel.CodeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListImportService extends ReferenceDataImportService<CodeListImportStatusProxy, CodeListImportProcess> {
	
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private RecordSessionManager sessionManager;
	
	@Secured("ROLE_ADMIN")
	public CodeListImportStatusProxy start(int codeListId, String tempFileName, CSVFileOptions fileOptions, boolean overwriteData) throws DataImportExeption {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			File importFile = new File(tempFileName);
			SessionStatus designerSessionStatus = sessionManager.getDesignerSessionStatus();
			CollectSurvey survey = designerSessionStatus.getSurvey();
			String langCode = designerSessionStatus.getCurrentLanguageCode();
			CodeList codeList = survey.getCodeListById(codeListId);
			importProcess = new CodeListImportProcess(codeListManager, codeList, langCode, importFile, fileOptions, overwriteData);
			importProcess.init();
			CodeListImportStatus status = importProcess.getStatus();
			if ( status != null && ! importProcess.getStatus().isError() ) {
				startProcessThread();
			}
		}
		return getStatus();
	}

	@Secured("ROLE_ADMIN")
	public CodeListImportStatusProxy getStatus() {
		if ( importProcess != null ) {
			CodeListImportStatus status = importProcess.getStatus();
			return new CodeListImportStatusProxy(status);
		} else {
			return null;
		}
	}
	
	
}
